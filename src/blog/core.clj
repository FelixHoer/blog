(ns blog.core
  (:use ring.adapter.jetty)
  (:use blog.constants)
  (:use blog.content.core)
  (:use blog.auth.core)
  (:use [compojure.core :only [defroutes]])
  (:require [ring.middleware.session :as session]
            [ring.middleware.params :as params]
            [compojure.route :as route]))

(defroutes blog-routes
  (var auth-routes)
  (var content-routes)
  (route/resources "/" {:root STATIC_RESOURCE_PATH})
  (route/not-found "Page not found"))

(def request-handler
  (-> blog-routes
      (session/wrap-session)
      (params/wrap-params)))

(defonce server
  (run-jetty (var request-handler) {:port 8080 :join? false}))

(defn -main [port]
  (println "main"))
