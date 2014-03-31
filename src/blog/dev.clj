(ns blog.dev
  (:use ring.adapter.jetty)
  (:use blog.core))

(defonce server
  (run-jetty (var blog-app) {:port 8080 :join? false}))
