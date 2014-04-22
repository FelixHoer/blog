(ns blog.theme.theme-handler-impl
  (:use [blog.theme.template :only [templates]]
        [compojure.core :only [routes]])
  (:require [compojure.route :as route]))

(def TEMPLATES {:article-list ["layout" "list"]
                :login        ["layout" "login"]})

(defn setup-templates [template-resource-path]
  (into {}
        (map (fn [[k ts]] [k (templates template-resource-path ts)])
             TEMPLATES)))

(defn setup-static-routes [static-resource-path]
  (routes
    (route/resources "/" {:root static-resource-path})
    (route/not-found "Page not found")))

(defn start-impl [{:keys [template-resource-path static-resource-path] :as this}]
  (assoc this
    :templates (setup-templates template-resource-path)
    :static-routes (setup-static-routes static-resource-path)))

(defn stop-impl [this]
  this)

(defn handle-impl [{templates :templates static-routes :static-routes}
                   {resp :resp :as req}]
  (cond
    (:template resp)
      (let [{template-key :template data :data} resp
            templ (template-key templates)]
        {:resp {:body (templ data)}})
    (:body resp)
      {:resp resp}
    :else
      {:resp (static-routes req)}))
