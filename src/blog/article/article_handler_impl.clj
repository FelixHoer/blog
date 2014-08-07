(ns blog.article.article-handler-impl
  (:require [compojure.core :refer [defroutes GET]]
            [clojure.string :as string]
            [blog.handler :as handler]
            [blog.text-plugin.plugin :as plugin]
            [blog.article.article-datastore :as ds]
            [blog.article.helpers :as helper]))


;;; plugins

(defn plugin-seq [component]
  (map #(% component) (:plugins component)))

(defn apply-plugins-page [component page]
  (let [plugins (plugin-seq component)
        transform-body #(update-in % [:body] plugin/apply-plugins plugins)]
    (update-in page [:items] #(map transform-body %))))


;;; server endpoints

(defn list-articles-page [{db :db :as component} page-num]
  (let [page (ds/article-page db page-num)
        overview (ds/article-overview db)]
    {:data (merge (apply-plugins-page component page)
                  (helper/pagination-urls #(str "/articles/page/" %) page)
                  overview)
     :template :article-list}))

(defn list-articles-month-page [{db :db :as component} month page-num]
  (let [page (ds/article-month-page db month page-num)
        overview (ds/article-overview db)]
    {:data (merge (apply-plugins-page component page)
                  (helper/pagination-urls #(str "/articles/month/" month "/page/" %) page)
                  overview)
     :template :article-list}))

(defn show-article [{db :db :as component} code]
  (let [page (ds/article db code)
        overview (ds/article-overview db)]
    {:data (merge (apply-plugins-page component page)
                  overview)
     :template :article}))


;;; routes

(defroutes content-routes
  ; all articles
  (GET "/"
       {component :component}
    (list-articles-page component 0))
  (GET "/articles"
       {component :component}
    (list-articles-page component 0))
  (GET "/articles/page/:page"
       {{page :page} :params component :component}
    (list-articles-page component (Integer/parseInt page)))

  ; articles of a month
  (GET "/articles/month/:month"
       {{month :month} :params component :component}
    (list-articles-month-page component month 0))
  (GET "/articles/month/:month/page/:page"
       {{month :month page :page} :params component :component}
    (list-articles-month-page component month (Integer/parseInt page)))

  ; single article
  (GET "/articles/:code"
       {{code :code} :params component :component}
    (show-article component code)))


;;; component

(defn handle [this next-handler req]
  (let [extended-req (assoc req :component this)
        resp (content-routes extended-req)
        next-req (update-in req [:resp] handler/deep-merge resp)]
    (next-handler next-req)))

(defn wrap-handler [this next-handler]
  #(handle this next-handler %))
