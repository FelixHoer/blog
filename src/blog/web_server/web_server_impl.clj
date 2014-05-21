(ns blog.web-server.web-server-impl
  (:use blog.handler)
  (:require [ring.middleware.session :as session]
            [ring.middleware.params :as params]
            [ring.middleware.flash :as flash]
            [ring.middleware.ssl :as ssl]
            [ring.middleware.anti-forgery :as forgery]
            [ring.util.anti-forgery :as forgery-util]
            [ring.util.response :as resp]
            [ring.util.request :as req]
            [ring.adapter.jetty :as jetty]))


; redirect http requests to https
; adapted from: https://github.com/ring-clojure/ring-ssl/blob/master/src/ring/middleware/ssl.clj

(defn- get-request? [{method :request-method}]
  (or (= method :head)
      (= method :get)))

(defn- request-url
  "Return the full URL of the request."
  [request]
  (str (-> request :scheme name)
       "://"
       (:server-name request)
       (if-let [port (:server-port request)]
         (if-not (#{80 443} port) (str ":" port)))
       (:uri request)
       (if-let [query (:query-string request)]
         (str "?" query))))

(defn wrap-ssl-redirect
  "Middleware that redirects any HTTP request to the equivalent HTTPS URL.

  Accepts the following options:

  :ssl-port - the request should be redirected to this port (defaults to the
              port of the original request)"
  [handler & [{:as options}]]
  (fn [request]
    (if (= (:scheme request) :https)
      (handler request)
      (-> request
          (assoc :scheme :https)
          (assoc :server-port (or (:ssl-port options) (:server-port request)))
          (request-url)
          (resp/redirect)
          (resp/status   (if (get-request? request) 301 307))))))


; prevent framing (clickjacking)

(defn wrap-anti-framing [handler]
  (fn [req]
    (let [resp (handler req)]
      (assoc-in resp [:headers "X-Frame-Options"] "DENY"))))


; prevent content type sniffing

(defn wrap-anti-content-type-sniffing [handler]
  (fn [req]
    (let [resp (handler req)]
      (assoc-in resp [:headers "X-Content-Type-Options"] "nosniff"))))


; content-type and charset middleware

(defn has-no-extension [{uri :uri}]
  (empty? (re-find #"\.[A-Za-z0-9]+$" uri)))

(defn wrap-content-type [handler]
  (fn [req]
    (let [{headers :headers :as resp} (handler req)
          content-type (get headers "Content-Type")]
      (if (and (empty? content-type)
               (has-no-extension req))
        (assoc-in resp [:headers "Content-Type"] "text/html;charset=UTF-8")
        resp))))


; extended request middleware

(defn wrap-extended-request [handler]
  (fn [req]
    (let [extended-req (assoc req :resp {:data nil :template nil})
          extended-resp (handler extended-req)]
      (:resp extended-resp))))


; anti-forgery, handles CSRF Tokens

(defn assoc-CSRF-field [req]
  (assoc-in req [:resp :data :CSRF-field] (forgery-util/anti-forgery-field)))

(defn wrap-anti-forgery-data [handler]
  (fn [req]
    (handler (assoc-CSRF-field req))))


; flash middleware

(defn flash-request [{flash :flash :as req}]
  (if flash
    (assoc-in req [:resp :data :flash] flash)
    req))

(defn redirect? [{status :status}]
  (and status
       (<= 300 status 399)))

(defn flash-response [{resp :resp :as req}]
  (if (redirect? resp)
    (if-let [flash (get-in resp [:data :flash])]
      (assoc-in req [:resp :flash] flash)
      req)
    req))

(defn wrap-flash-data [handler]
  (fn [req]
    (let [f-req (flash-request req)
          h-req (handler f-req)]
      (flash-response h-req))))


; component middleware

(defn wrap-handler-component [handler]
  (fn [req]
    (handle handler req)))


; webserver definition

(defn wrap-normal-middleware [handler {ssl :ssl}]
  (-> handler
      (wrap-handler-component)
      (wrap-flash-data)
      (wrap-anti-forgery-data)
      (wrap-extended-request)
      (wrap-content-type)
      (flash/wrap-flash)
      (forgery/wrap-anti-forgery)
      (session/wrap-session {:cookie-attrs {:secure (boolean ssl)
                                            :http-only true}})
      (params/wrap-params)
      (wrap-anti-content-type-sniffing)
      (wrap-anti-framing)))

(defn wrap-ssl-middleware [handler {ssl :ssl}]
  (if ssl
    (-> handler
        (ssl/wrap-hsts)
        (ssl/wrap-forwarded-scheme)
        (wrap-ssl-redirect (select-keys ssl #{:ssl-port})))
    handler))

(defn make-handler [handler this]
  (-> handler
      (wrap-normal-middleware this)
      (wrap-ssl-middleware this)))

(defn start-impl [{old-server :server next :next port :port ssl :ssl :as this}]
  (if old-server
    this
    (let [handler (make-handler next this)
          options (merge {:port (or port 80)
                          :join? false}
                         (if ssl
                           (-> (select-keys ssl #{:ssl-port :keystore :key-password})
                               (assoc :ssl? true))))
          server (jetty/run-jetty handler options)]
      (assoc this :server server))))


(defn stop-impl [{server :server :as this}]
  (if server
    (do
      (.stop server)
      (assoc this :server nil))
    this))
