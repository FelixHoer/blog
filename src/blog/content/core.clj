(ns blog.content.core
  (:use compojure.core)
  (:use blog.constants)
  (:use blog.template)
  (:use blog.content.helpers)
  (:use blog.content.data)
  (:use [blog.auth.core :only [authenticated]])
  (:require [clojure.string :as string]))


; templates

(def list-template    (templates "layout" "list"))
(def article-template (templates "layout" "article"))

(def article-partial (templates "article"))


; server endpoints

(defn list-articles-page [page session]
  (println "list all" page)
  (let [files (article-files)
        page (article-page-data page files)
        data (merge (pagination-urls #(str "/articles/page/" %) page)
                    {:body (string/join (map article-partial (:items page)))}
                    (sidebar-data files))]
    {:body (list-template data)}))

(defn list-articles-month-page [month page session]
  (println "list month" month page)
  (let [files (article-files)
        page (article-month-page-data month page files)
        data (merge (pagination-urls #(str "/articles/month/" month "/page/" %) page)
                    {:body (string/join (map article-partial (:items page)))}
                    (sidebar-data files))]
    {:body (list-template data)}))

(defn show-article [code session]
  (println "show" code)
  (let [files (article-files)
        data (merge (sidebar-data files)
                    {:body (article-partial (article-data (code->filename code)))})]
    {:body (list-template data)}))


; routes

(defroutes content-routes
  (GET "/"
       {session :session :as req}
    (authenticated req
      (list-articles-page 0 session)))
  (GET "/articles"
       {session :session :as req}
    (authenticated req
      (list-articles-page 0 session)))
  (GET "/articles/page/:page"
       {{page :page} :params session :session :as req}
    (authenticated req
      (list-articles-page (Integer/parseInt page) session)))
  (GET "/articles/month/:month"
       {{month :month} :params session :session :as req}
    (authenticated req
      (list-articles-month-page month 0 session)))
  (GET "/articles/month/:month/page/:page"
       {{month :month page :page} :params session :session :as req}
    (authenticated req
      (list-articles-month-page month (Integer/parseInt page) session)))
  (GET "/articles/:code"
       {{code :code} :params session :session :as req}
    (authenticated req
      (show-article code session))))
