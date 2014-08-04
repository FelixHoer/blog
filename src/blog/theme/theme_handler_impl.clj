(ns blog.theme.theme-handler-impl
  (:require [compojure.core :refer [routes]]
            [compojure.route :as route]
            [selmer.parser :as selmer]
            [clojure.string :as string]))


;;; constants

(def TEMPLATES [:error
                :404
                :login
                :article-list
                :article])

;;; templates

(defn keyword->underscore-name [k]
  (string/replace (name k) \- \_))

(defn keyword->template-path [k template-resource-path]
  (str template-resource-path "/" (keyword->underscore-name k) ".html"))

(defn create-template
  ([k res-path]
   (-> (keyword->template-path k res-path)
       create-template))
  ([path]
   (fn [data] (selmer/render-file path data))))


;;; setup / lifecycle

(defn setup-templates [res-path]
  (into {}
        (map (fn [k] [k (create-template k res-path)])
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

(defn handle [{templates :templates :as this} next-handler req]
  (if-let [static (static-response this req)]
    static
    (try
      (let [resp (next-handler req)]
        (dynamic-response this req resp))
      (catch Exception e
        (.printStackTrace e)
        {:body ((:error templates) {})}))))

(defn wrap-handler [this next-handler]
  #(handle this next-handler %))
