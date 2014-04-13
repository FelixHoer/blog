(ns blog.theme.theme-handler-impl
  (:use [blog.theme.template :only [templates]]
        blog.constants
        [compojure.core :only [defroutes]])
  (:require [compojure.route :as route]))


(def TEMPLATES {:article-list (templates "layout" "list")})

(defroutes static-routes
  (route/resources "/" {:root STATIC_RESOURCE_PATH})
  (route/not-found "Page not found"))

(defn handle-impl [this req]
  (println "themer" req)
  (let [{{data :data template-key :template} :resp} req]
    (if template-key
      {:resp {:body ((template-key TEMPLATES) data)}}
      {:resp (static-routes req)})))
