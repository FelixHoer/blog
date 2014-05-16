(ns blog.web-server.web-server-impl
  (:use blog.handler)
  (:require [ring.middleware.session :as session]
            [ring.middleware.params :as params]
            [ring.middleware.flash :as flash]
            [ring.adapter.jetty :as jetty]))


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

(defn wrap-flash [handler]
  (fn [req]
    (let [f-req (flash-request req)
          h-req (handler f-req)]
      (flash-response h-req))))


; component middleware

(defn wrap-handler-component [handler]
  (fn [req]
    (handle handler req)))


; webserver definition

(defn make-handler [handler]
  (-> handler
      (wrap-handler-component)
      (wrap-flash)
      (wrap-extended-request)
      (wrap-content-type)
      (flash/wrap-flash)
      (session/wrap-session {:cookie-attrs {;:secure true
                                            :http-only true}})
      (params/wrap-params)
      (wrap-anti-content-type-sniffing)
      (wrap-anti-framing)))

(defn start-impl [{old-server :server next :next port :port :as this}]
  (if old-server
    this
    (let [handler (make-handler next)
          server (jetty/run-jetty handler {:port (or port 8080)
                                           :join? false})]
      (assoc this :server server))))

(defn stop-impl [{server :server :as this}]
  (if server
    (do
      (.stop server)
      (assoc this :server nil))
    this))
