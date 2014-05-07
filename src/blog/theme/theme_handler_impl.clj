(ns blog.theme.theme-handler-impl
  (:use [blog.theme.template :only [templates]]
        [compojure.core :only [routes]]
        blog.handler)
  (:require [compojure.route :as route]))

(def TEMPLATES {:error        ["layout" "error"]
                :404          ["layout" "404"]
                :login        ["layout" "login"]
                :article-list ["layout" "blog_layout" "article_list"]
                :article      ["layout" "blog_layout" "article"]})

(defn setup-templates [template-resource-path]
  (into {}
        (map (fn [[k ts]] [k (templates template-resource-path ts)])
             TEMPLATES)))

(defn setup-static-routes [static-resource-path]
  (routes
    (route/resources "/" {:root static-resource-path})))

(defn start-impl [{:keys [template-resource-path static-resource-path] :as this}]
  (assoc this
    :templates (setup-templates template-resource-path)
    :static-routes (setup-static-routes static-resource-path)))

(defn stop-impl [this]
  this)

(defn dynamic-response [{:keys [templates next]} req]
  (let [{resp :resp} (handle next req)]
    (cond
     (:template resp)
       (let [{template-key :template data :data} resp
             template (template-key templates)
             extended-data (assoc data :session (:session req))]
         {:body (template extended-data)})
     (:body resp)
       resp
     :else
       {:status 404
        :body ((:404 templates) {})})))

(defn theme-response [{:keys [templates static-routes] :as this} req]
  (if-let [static-resp (static-routes req)]
    static-resp
    (try
      (dynamic-response this req)
      (catch Exception e
        (.printStackTrace e)
        {:body ((:error templates) {})}))))

(defn handle-impl [this req]
  (update-in req [:resp] deep-merge (theme-response this req)))
