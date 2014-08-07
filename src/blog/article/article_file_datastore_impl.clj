(ns blog.article.article-file-datastore-impl
  (:require [clojure.string :as string]
            [markdown.core :as markdown]
            [blog.article.helpers :as help]))


;;; constants

(def DATE_PREFIX_LENGTH 7)

(def EXTENSION ".md")
(def EXTENSION_LENGTH (count EXTENSION))


;;; helper

(defn group-by-month [files]
  (let [date-prefix-f #(string/join (take DATE_PREFIX_LENGTH %))
        grouped-files (group-by date-prefix-f files)]
    grouped-files))

(defn code->filename [code]
  (str code EXTENSION))

(defn filename->code [filename]
  (string/join (drop-last EXTENSION_LENGTH filename)))

(defn parse-article-code [code]
  (help/complete-article {:code code}))

(def parse-article-filename (comp parse-article-code filename->code))


;;; list of article names

(defn article-files [{article-path :article-path}]
  (let [root (clojure.java.io/file article-path)
        files (filter #(.isFile %) (file-seq root))]
    (map #(.getName %) files)))


;;; content of an article

(defn article-data [{article-path :article-path} name]
  (let [safe-name (string/join (remove #(= \/ %) name))
        path (str article-path "/" safe-name)
        file-content (slurp path)]
    (merge (parse-article-filename name)
           {:body file-content})))


;;; paginated article lists

(defn article-page-data [db page-num article-files]
  (let [sorted-files (reverse (sort article-files))
        data (help/paginate page-num (:articles-per-page db) sorted-files)]
    (update-in data [:items]
               (fn [item] (map #(article-data db %) item)))))

(defn article-month-page-data [db month page-num article-files]
  (let [month-files (get (group-by-month article-files) month)]
    (article-page-data db page-num month-files)))


;;; component

(defn article [this code]
  (let [files (article-files this)
        articles (try
                   [(article-data this (code->filename code))]
                   (catch Exception e
                     []))]
    {:items articles}))


(defn article-page [this page-num]
  (->> (article-files this)
       (article-page-data this page-num)))

(defn article-month-page [this month page-num]
  (->> (article-files this)
       (article-month-page-data this month page-num)))


;;; sidebar

(defn recent-article-data [{recent-articles :recent-articles} article-files]
  (let [sorted-files (reverse (sort article-files))
        recent-files (take recent-articles sorted-files)]
    (map parse-article-filename recent-files)))

(defn archive-data [this article-files]
  (let [months (keys (group-by-month article-files))
        sorted-months (reverse (sort months))]
    (map parse-article-code sorted-months)))

(defn article-overview [this]
  (let [files (article-files this)]
    {:recent-articles (recent-article-data this files)
     :archive-months (archive-data this files)}))
