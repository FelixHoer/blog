(ns blog.article.article-file-datastore-impl
  (:use blog.constants)
  (:use blog.article.helpers)
  (:require [clojure.string :as string]
            [markdown.core :as markdown]))


; helper

(defn group-by-month [files]
  (let [date-prefix-f #(string/join (take DATE_PREFIX_LENGTH %))
        grouped-files (group-by date-prefix-f files)]
    grouped-files))


; list of article names

(defn article-files []
  (let [root (clojure.java.io/file ARTICLES_PATH)
        files (filter #(.isFile %) (file-seq root))]
    (map #(.getName %) files)))


; content of an article

(defn article-data [name]
  (let [safe-name (string/join (remove #(= \/ %) name))
        path (str ARTICLES_PATH "/" safe-name)
        file-content (slurp path)
        html (markdown/md-to-html-string file-content)]
    (merge (parse-article-filename name) {:body html})))


; paginated main list

(defn article-page-data [page-num article-files]
  (let [sorted-files (reverse (sort article-files))
        data (paginate page-num ARTICLES_PER_PAGE sorted-files)]
    (update-in data [:items] #(map article-data %))))

(defn article-month-page-data [month page-num article-files]
  (let [month-files (get (group-by-month article-files) month)]
    (article-page-data page-num month-files)))


; sidebar

(defn sidebar-recent-article-data [article-files]
  (let [sorted-files (reverse (sort article-files))
        recent-files (take RECENT_ARTICLES sorted-files)]
    (map parse-article-filename recent-files)))

(defn sidebar-archive-data [article-files]
  (let [months (keys (group-by-month article-files))
        sorted-months (reverse (sort months))]
    (map parse-article-code sorted-months)))

(defn sidebar-data [article-files]
  {:recent-articles (sidebar-recent-article-data article-files)
   :archive-months (sidebar-archive-data article-files)})


; component

(defn article-impl [this name]
  (merge {:items (article-data name)}
         (sidebar-data (article-files))))

(defn article-page-impl [this page-num]
  (let [files (article-files)]
    (merge (article-page-data page-num files)
           (sidebar-data files))))

(defn article-month-page-impl [this month page-num]
  (let [files (article-files)]
    (merge (article-month-page-data month page-num files)
           (sidebar-data files))))
