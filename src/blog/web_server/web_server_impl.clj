(ns blog.web-server.web-server-impl
  (:use blog.handler)
  (:require [ring.middleware.session :as session]
            [ring.middleware.params :as params]
            [ring.adapter.jetty :as jetty]))

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

(defn wrap-handler [handler]
  (fn [req]
    (let [extended-req (assoc req :resp {:data nil :template nil})
          extended-resp (handle handler extended-req)]
      (:resp extended-resp))))

(defn make-handler [handler]
  (-> (wrap-handler handler)
      (wrap-content-type)
      (session/wrap-session)
      (params/wrap-params)))

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
