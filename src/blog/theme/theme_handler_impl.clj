(ns blog.theme.theme-handler-impl
  (:use [blog.theme.template :only [templates]]
        blog.constants
        [compojure.core :only [defroutes]])
  (:require [compojure.route :as route]))


(def TEMPLATES {:article-list (templates "layout" "list")
                :login        (templates "layout" "login")})

(defroutes static-routes
  (route/resources "/" {:root STATIC_RESOURCE_PATH})
  (route/not-found "Page not found"))

(defn handle-impl [this {resp :resp :as req}]
  (cond
    (:template resp)
      (let [{template-key :template data :data} resp]
        {:resp {:body ((template-key TEMPLATES) data)}})
    (:body resp)
      {:resp resp}
    :else
      {:resp (static-routes req)}))
