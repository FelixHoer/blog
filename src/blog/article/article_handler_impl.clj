(ns blog.article.article-handler-impl
  (:use compojure.core
        blog.handler
        blog.article.article-datastore
        blog.article.helpers)
  (:require [clojure.string :as string]))


; server endpoints

(defn list-articles-page [{db :db} page-num]
  (let [page (article-page db page-num)]
    {:data (merge page
                  (pagination-urls #(str "/articles/page/" %) page))
     :template :article-list}))

(defn list-articles-month-page [{db :db} month page-num]
  (let [page (article-month-page db month page-num)]
    {:data (merge page
                  (pagination-urls #(str "/articles/month/" month "/page/" %) page))
     :template :article-list}))

; TODO move code->filename to the db
(defn show-article [{db :db} code]
  {:data (article db (code->filename code))
   :template :article-list})


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

(defn handle-impl [this req]
  (let [extended-req (assoc req :component this)
        resp (content-routes extended-req)
        next (:next  this)
        next-req (update-in req [:resp] merge resp)]
    (handle next next-req)))
