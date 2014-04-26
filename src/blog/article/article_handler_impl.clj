(ns blog.article.article-handler-impl
  (:use compojure.core
        blog.handler
        [blog.article.article-datastore :only [article-page article-month-page article]]
        [blog.article.helpers :only [pagination-urls]]
        [blog.article-plugin.plugin :only [process]])
  (:require [clojure.string :as string]))


; plugins

(defn plugin-seq [component]
  (map #(% component) (:plugins component)))

(defn apply-plugins [body plugins]
  (reduce (fn [current-body plugin] (process plugin current-body))
          body
          plugins))

(defn apply-plugins-page [component page]
  (let [plugins (plugin-seq component)
        transform-body #(update-in % [:body] apply-plugins plugins)]
    (update-in page [:items] #(map transform-body %))))


; server endpoints

(defn list-articles-page [{db :db :as component} page-num]
  (let [page (article-page db page-num)]
    {:data (merge (apply-plugins-page component page)
                  (pagination-urls #(str "/articles/page/" %) page))
     :template :article-list}))

(defn list-articles-month-page [{db :db :as component} month page-num]
  (let [page (article-month-page db month page-num)]
    {:data (merge (apply-plugins-page component page)
                  (pagination-urls #(str "/articles/month/" month "/page/" %) page))
     :template :article-list}))

(defn show-article [{db :db :as component} code]
  (let [page (article db code)]
    {:data (apply-plugins-page component page)
     :template :article}))


; routes

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


; component

(defn start-impl [this]
  this)

(defn stop-impl [this]
  this)

(defn handle-impl [{next :next :as this} req]
  (let [extended-req (assoc req :component this)
        resp (content-routes extended-req)
        next-req (update-in req [:resp] merge resp)]
    (if next
      (handle next next-req)
      next-req)))
