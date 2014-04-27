(ns blog.comment.comment-sql-datastore-impl
  (:require [clojure.java.jdbc :as jdbc]
            [blog.comment.comment-datastore :as ds]))


; setup operations

(defn create-comment-table [{db :db}]
  (jdbc/db-do-commands db
    (jdbc/create-table-ddl :comment
                           [:name    "varchar(255)"]
                           [:time    "date"]
                           [:text    "text"]
                           [:article "varchar(255)"])))


; datastore operations

(defn insert-comment [{db :db} {:keys [name text time]} article-code]
  (jdbc/insert! db :comment {:name name
                             :time time
                             :text text
                             :article article-code}))

(defn select-comment-count [db article-code]
  (jdbc/query db [(str "SELECT COUNT(*) "
                       "FROM comment "
                       "WHERE article = ?")
                  article-code]
              :row-fn :c1 ; first column is the count
              :result-set-fn first))

(defn select-comment-counts [{db :db} article-codes]
  (jdbc/with-db-connection [db-con db]
    (->> article-codes
         (map (fn [c] [c (select-comment-count db-con c)]))
         (into {}))))

(defn select-comments [{db :db} article-code]
  (jdbc/query db [(str "SELECT name, time, text "
                       "FROM comment "
                       "WHERE article = ? "
                       "ORDER BY time DESC")
                  article-code]
              :row-fn ds/map->Comment))


; component

(defn save-comment-impl [this comment article-code]
  (insert-comment this comment article-code))

(defn read-comment-counts-impl [this article-codes]
  (select-comment-counts this article-codes))

(defn read-comments-impl [this article-code]
  (select-comments this article-code))
