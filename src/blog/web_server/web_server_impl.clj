(ns blog.web-server.web-server-impl
  (:require [ring.middleware.session :as session]
            [ring.middleware.params :as params]
            [ring.middleware.flash :as flash]
            [ring.middleware.ssl :as ssl]
            [ring.middleware.anti-forgery :as forgery]
            [ring.util.anti-forgery :as forgery-util]
            [ring.util.response :as resp]
            [ring.util.request :as req]
            [ring.adapter.jetty :as jetty]
            [clojure.string :as str]
            [blog.handler :as h]))


;;; debug-tracer

(defn wrap-tracer [handler name]
  (fn [req]
    (println "tracer-req" name req)
    (let [resp (handler req)]
      (println "tracer-resp" name resp)
      resp)))


;;; prevent framing (clickjacking)

(defn wrap-anti-framing [handler]
  (fn [req]
    (let [resp (handler req)]
      (assoc-in resp [:headers "X-Frame-Options"] "DENY"))))


;;; prevent content type sniffing

(defn wrap-anti-content-type-sniffing [handler]
  (fn [req]
    (let [resp (handler req)]
      (assoc-in resp [:headers "X-Content-Type-Options"] "nosniff"))))


;;; define a content security polics

(defn build-csp [csp]
  (->> csp
      (map (fn [[k v]] (str/join " " (cons (name k) v))))
      (str/join "; ")))

(defn wrap-content-security-policy [handler csp]
  (if-not csp
    handler
    (fn [req]
      (let [resp (handler req)]
        (assoc-in resp [:headers "Content-Security-Policy"] (build-csp csp))))))


;;; content-type and charset middleware

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


;;; extended request middleware

(defn wrap-extended-request [handler]
  (fn [req]
    (-> req
        (assoc :resp {:data nil :template nil})
        handler)))


;;; anti-forgery, handles CSRF Tokens

(defn assoc-CSRF-field [req]
  (assoc-in req [:resp :data :CSRF-field] (forgery-util/anti-forgery-field)))

(defn wrap-anti-forgery [handler]
  (forgery/wrap-anti-forgery
   (fn [req]
     (handler (assoc-CSRF-field req)))))


;;; flash middleware

(defn flash-request [{flash :flash :as req}]
  (if flash
    (assoc-in req [:resp :data :flash] flash)
    req))

(defn redirect? [{status :status}]
  (and status
       (<= 300 status 399)))

(defn flash-response [resp]
  (if-not (redirect? resp)
    resp
    (if-let [flash (get-in resp [:data :flash])]
      (assoc resp :flash flash)
      resp)))

(defn wrap-flash [handler]
  (flash/wrap-flash
   (fn [req]
     (-> req
         flash-request
         handler
         flash-response))))


;;; middleware composition

(defn wrap-component-middleware [{handler-keys :handlers :as this}]
  (let [handlers (map #(% this) handler-keys)
        last-handler :resp
        wrap-next (fn [accu handler] (h/wrap-handler handler accu))]
    (reduce wrap-next last-handler (reverse handlers))))

(defn wrap-normal-middleware [handler {csp :csp ssl :ssl}]
  (-> handler
      (wrap-flash)
      (wrap-anti-forgery)
      (wrap-extended-request)
      (wrap-content-type)
      (session/wrap-session {:cookie-attrs {:secure (boolean ssl)
                                            :http-only true}})
      (params/wrap-params)
      (wrap-content-security-policy csp)
      (wrap-anti-content-type-sniffing)
      (wrap-anti-framing)))

(defn wrap-ssl-middleware [handler {{reverse-proxy? :via-reverse-proxy? :as ssl} :ssl}]
  (if-not ssl
    handler
    (as-> handler h
          (ssl/wrap-hsts h)
          (ssl/wrap-ssl-redirect h (select-keys ssl #{:ssl-port}))
          (if reverse-proxy?
            (ssl/wrap-forwarded-scheme h)
            h))))

(defn make-handler [this]
  (-> (wrap-component-middleware this)
      (wrap-normal-middleware this)
      (wrap-ssl-middleware this)))


;;; lifecycle methods, start/stop

(defn start [{old-server :server port :port ssl :ssl :as this}]
  (if old-server
    this
    (let [handler (make-handler this)
          options (merge {:port (or port 80)
                          :join? false}
                         (if (and ssl (not (:via-reverse-proxy? ssl)))
                           (-> (select-keys ssl #{:ssl-port :keystore :key-password})
                               (assoc :ssl? true))))
          server (jetty/run-jetty handler options)]
      (assoc this :server server))))


(defn stop [{server :server :as this}]
  (if server
    (do
      (.stop server)
      (assoc this :server nil))
    this))
