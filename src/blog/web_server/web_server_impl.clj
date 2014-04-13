(ns blog.web-server.web-server-impl
  (:use blog.handler)
  (:require [ring.middleware.session :as session]
            [ring.middleware.params :as params]
            [ring.adapter.jetty :as jetty]))

(defn wrap-handler [handler]
  (fn [req]
    (println "handler" req)
    (let [extended-req (assoc req :resp {:data nil :template nil})
          extended-resp (handle handler extended-req)]
      (:resp extended-resp))))

(defn make-handler [handler]
  (-> (wrap-handler handler)
      (session/wrap-session)
      (params/wrap-params)))

(defn start-impl [this]
  (println "start WebServer")
  (if (:jetty this)
    this
    (let [handler (make-handler (:next this))
          port (if (:port this) (:port this) 8080)
          server (jetty/run-jetty handler {:port  port :join? false})]
      (assoc this :jetty server))))

(defn stop-impl [this]
  (println "stop WebServer")
  this)
