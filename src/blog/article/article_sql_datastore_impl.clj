(ns blog.article.article-sql-datastore-impl
  (:use blog.article.helpers)
  (:require [clojure.java.jdbc :as jdbc]))


; setup operations

(defn create-article-table [{db :db}]
  (jdbc/db-do-commands db
    (jdbc/create-table-ddl :article
                           [:name "varchar(255)"]
                           [:date "date"]
                           [:body "text"]))
  :ok)


; add article

(defn add-article [{db :db} {:keys [name date body]}]
  (jdbc/insert! db :article {:name name
                             :date date
                             :body body})
  :ok)


; helpers

(defn build-article-item [{:keys [name body]}]
  (merge (parse-article-code name)
         {:body body}))


; paginated main list

(defn select-article-page [{:keys [db articles-per-page]} page-num]
  (jdbc/query db [(str "SELECT name, body "
                       "FROM article "
                       "ORDER BY date "
                       "LIMIT ? "
                       "OFFSET ?")
                  articles-per-page
                  (* articles-per-page page-num)]
              :row-fn build-article-item))

(defn select-article-count [{db :db}]
  (jdbc/query db [(str "SELECT COUNT(*) "
                       "FROM article")]
              :row-fn :c1
              :result-set-fn first))

(defn article-page [{articles-per-page :articles-per-page :as this} page-num]
  (let [items (select-article-page this page-num)
        article-count (select-article-count this)
        has-next? (< (* (inc page-num) articles-per-page) article-count)
        has-previous? (pos? page-num)]
    (merge {:current-page page-num
            :items items}
           (if has-previous? {:previous-page (dec page-num)})
           (if has-next?     {:next-page     (inc page-num)}))))


; paginated month list

(defn select-article-month-page [{:keys [db articles-per-page]} page-num {:keys [month year]}]
  (jdbc/query db [(str "SELECT name, body "
                       "FROM article "
                       "WHERE MONTH(date) = ? AND YEAR(date) = ? "
                       "ORDER BY date "
                       "LIMIT ? "
                       "OFFSET ?")
                  month
                  year
                  articles-per-page
                  (* articles-per-page page-num)]
              :row-fn build-article-item))

(defn select-article-month-count [{db :db} {:keys [month year]}]
  (jdbc/query db [(str "SELECT COUNT(*) "
                       "FROM article "
                       "WHERE MONTH(date) = ? AND YEAR(date) = ?")
                  month
                  year]
              :row-fn :c1
              :result-set-fn first))

(defn article-month-page [{articles-per-page :articles-per-page :as this} month page-num]
  (let [date (parse-article-code month)
        items (select-article-month-page this page-num date)
        article-count (select-article-month-count this date)
        has-next? (< (* (inc page-num) articles-per-page) article-count)
        has-previous? (pos? page-num)]
    (merge {:current-page page-num
            :items items}
           (if has-previous? {:previous-page (dec page-num)})
           (if has-next?     {:next-page     (inc page-num)}))))


; single article

(defn select-article [{db :db} code]
  (jdbc/query db [(str "SELECT name, body "
                       "FROM article "
                       "WHERE name = ?")
                  code]
              :row-fn build-article-item))

(defn article [this code]
  {:items (select-article this code)
   :current-page 0})


; sidebar

(defn select-recent-articles [{db :db recent-articles :recent-articles}]
  (jdbc/query db [(str "SELECT name "
                       "FROM article "
                       "ORDER BY date "
                       "LIMIT ?")
                  recent-articles]
              :row-fn (comp parse-article-code :name)))

(defn parse-article-date [{month :c1 year :c2}]
  (let [code (format "%4d-%02d" year month)]
    (parse-article-code code)))

(defn select-archive-months [{db :db}]
  (jdbc/query db [(str "SELECT DISTINCT MONTH(date), YEAR(date), date "
                       "FROM article "
                       "ORDER BY date")]
              :row-fn parse-article-date))

(defn sidebar [this]
  {:recent-articles (select-recent-articles this)
   :archive-months (select-archive-months this)})


; component

(defn article-impl [this code]
  (merge (article this code)
         (sidebar this)))

(defn article-page-impl [this page-num]
  (merge (article-page this page-num)
         (sidebar this)))

(defn article-month-page-impl [this month page-num]
  (merge (article-month-page this month page-num)
         (sidebar this)))
