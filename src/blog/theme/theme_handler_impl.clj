(ns blog.theme.theme-handler-impl
  (:use [blog.theme.template :only [templates]]
        [compojure.core :only [routes]]
        blog.handler)
  (:require [compojure.route :as route]))

(def TEMPLATES {:error        ["layout" "error"]
                :login        ["layout" "login"]
                :article-list ["layout" "list"]})

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

(defn handle-impl [{:keys [templates static-routes next]} req]
  (try
    (let [{resp :resp} (handle next req)]
      (cond
        (:template resp)
          (let [{template-key :template data :data} resp
                template (template-key templates)]
            {:resp {:body (template data)}})
        (:body resp)
          {:resp resp}
        :else
          {:resp (static-routes req)}))
    (catch Exception e
      (.printStackTrace e)
      {:resp {:body ((:error templates) {})}})))
