(ns blog.theme.theme-handler-impl
  (:use [blog.theme.template :only [templates]]
        [compojure.core :only [routes]])
  (:require [compojure.route :as route]))


;;; constants

(def TEMPLATES {:error        ["layout" "error"]
                :404          ["layout" "404"]
                :login        ["layout" "login"]
                :article-list ["layout" "blog_layout" "article_list"]
                :article      ["layout" "blog_layout" "article"]})


;;; setup / lifecycle

(defn setup-templates [template-resource-path]
  (into {}
        (map (fn [[k ts]] [k (templates template-resource-path ts)])
             TEMPLATES)))

(defn setup-static-routes [static-resource-path]
  (routes
    (route/resources "/" {:root static-resource-path})))

(defn start [{:keys [template-resource-path static-resource-path] :as this}]
  (assoc this
    :templates (setup-templates template-resource-path)
    :static-routes (setup-static-routes static-resource-path)))

(defn stop [this]
  this)


;;; handler

(defn static-response [{static-routes :static-routes} req]
  (static-routes req))

(defn dynamic-response [{:keys [templates next]} req resp]
  (cond
   (:template resp)
     (let [{template-key :template data :data} resp
           template (template-key templates)
           extended-data (assoc data :session (:session req))]
       (assoc resp :body (template extended-data)))
   (:body resp)
     resp
   :else
     (merge resp {:status 404
                  :body ((:404 templates) {})})))

(defn handle [this next-handler req]
  (if-let [static (static-response this req)]
    static
    (let [resp (next-handler req)]
      (try
        (dynamic-response this req resp)
        (catch Exception e
          (.printStackTrace e)
          {:body ((:error templates) {})})))))

(defn wrap-handler [this next-handler]
  #(handle this next-handler %))
