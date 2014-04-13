(ns blog.content.core
  (:use compojure.core)
  (:use blog.handler)
  (:use blog.constants)
  (:use blog.template)
  (:use blog.content.helpers)
  (:use blog.content.data)
  (:use [blog.auth.core :only [authenticated]])
  (:require [clojure.string :as string]
            [com.stuartsierra.component :as component]))


; server endpoints

(defn list-articles-page [{db :db} page-num]
  (println "list all" page-num)
  (let [page (article-page db page-num)]
    {:data (merge page
                  (pagination-urls #(str "/articles/page/" %) page))
     :template :article-list}))

(defn list-articles-month-page [{db :db} month page-num]
  (println "list month" month page-num)
  (let [page (article-month-page db month page-num)]
    {:data (merge page
                  (pagination-urls #(str "/articles/month/" month "/page/" %) page))
     :template :article-list}))

; TODO move code->filename to the db
(defn show-article [{db :db} code]
  (println "show" code)
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

(defrecord ArticleHandler [db next]
  component/Lifecycle
    (start [this]
      (println "start")
      this)
    (stop [this]
      (println "stop")
      this)
  Handler
    (handle [this req]
      (println "article-handler" req)
      (let [extended-req (assoc req :component this)
            resp (content-routes extended-req)
            next-req (update-in req [:resp] merge resp)]
        (handle next next-req))))

(defn new-article-handler []
  (map->ArticleHandler {}))

