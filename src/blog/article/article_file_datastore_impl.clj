(ns blog.article.article-file-datastore-impl
  (:use blog.article.helpers)
  (:require [clojure.string :as string]
            [markdown.core :as markdown]))


; constants

(def DATE_PREFIX_LENGTH 7)

(def EXTENSION ".md")
(def EXTENSION_LENGTH (count EXTENSION))


; helper

(defn group-by-month [files]
  (let [date-prefix-f #(string/join (take DATE_PREFIX_LENGTH %))
        grouped-files (group-by date-prefix-f files)]
    grouped-files))

(defn code->filename [code]
  (str code EXTENSION))

(defn filename->code [filename]
  (string/join (drop-last EXTENSION_LENGTH filename)))

(def parse-article-filename (comp parse-article-code filename->code))


; list of article names

(defn article-files [{article-path :article-path}]
  (let [root (clojure.java.io/file article-path)
        files (filter #(.isFile %) (file-seq root))]
    (map #(.getName %) files)))


; content of an article

(defn article-data [{article-path :article-path} name]
  (let [safe-name (string/join (remove #(= \/ %) name))
        path (str article-path "/" safe-name)
        file-content (slurp path)]
    (merge (parse-article-filename name)
           {:body file-content})))


; paginated main list

(defn article-page-data [db page-num article-files]
  (let [sorted-files (reverse (sort article-files))
        data (paginate page-num (:articles-per-page db) sorted-files)]
    (update-in data [:items]
               (fn [item] (map #(article-data db %) item)))))

(defn article-month-page-data [db month page-num article-files]
  (let [month-files (get (group-by-month article-files) month)]
    (article-page-data db page-num month-files)))


; sidebar

(defn sidebar-recent-article-data [{recent-articles :recent-articles} article-files]
  (let [sorted-files (reverse (sort article-files))
        recent-files (take recent-articles sorted-files)]
    (map parse-article-filename recent-files)))

(defn sidebar-archive-data [db article-files]
  (let [months (keys (group-by-month article-files))
        sorted-months (reverse (sort months))]
    (map parse-article-code sorted-months)))

(defn sidebar-data [db article-files]
  {:recent-articles (sidebar-recent-article-data db article-files)
   :archive-months (sidebar-archive-data db article-files)})


; component

(defn article-impl [this code]
  (let [files (article-files this)
        articles (try
                   [(article-data this (code->filename code))]
                   (catch Exception e
                     []))]
    (merge {:items articles}
           (sidebar-data this files))))

(defn article-page-impl [this page-num]
  (let [files (article-files this)]
    (merge (article-page-data this page-num files)
           (sidebar-data this files))))

(defn article-month-page-impl [this month page-num]
  (let [files (article-files this)]
    (merge (article-month-page-data this month page-num files)
           (sidebar-data this files))))
