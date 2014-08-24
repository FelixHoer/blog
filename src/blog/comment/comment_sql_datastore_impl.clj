(ns blog.comment.comment-sql-datastore-impl
  (:require [clojure.java.jdbc :as jdbc]
            [blog.comment.comment-datastore :as spec]
            [clojure.string :as string]))


;;;; setup operations ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn create-comment-table [{db :db}]
  (try 
    (jdbc/db-do-commands db
      (jdbc/create-table-ddl :comment
                             [:id      "serial"]
                             [:name    "varchar(255)"]
                             [:time    "datetime"]
                             [:text    "text"]
                             [:article "varchar(255)"]))
    :ok
  (catch Exception e :fail)))


;;;; management operations ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; delete comment

(defn delete-comment [{db :db} comment-id]
  (jdbc/delete! db :comment ["id = ?" comment-id])
  :ok)


;;;; usage operations ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; select comment counts (for multiple articles)

(defn select-comment-count [db article-code]
  (jdbc/query db [(str "SELECT COUNT(*) "
                       "FROM comment "
                       "WHERE article = ?")
                  article-code]
              :row-fn :c1 ; first column is the count
              :result-set-fn first))

(defmacro with-reused-db-connection [[con db] & forms]
  `(if (:connection ~db)
    (let [~con ~db]
      ~@forms)
    (jdbc/with-db-connection [~con ~db]
      ~@forms)))

(defn select-comment-counts [{db :db} article-codes]
  (with-reused-db-connection [con db]
    (->> article-codes
         (map (fn [c] [c (select-comment-count con c)]))
         (into {}))))


;;; select comments

(defn select-comments [{db :db} article-code]
  (jdbc/query db [(str "SELECT id, name, time, text "
                       "FROM comment "
                       "WHERE article = ? "
                       "ORDER BY time DESC")
                  article-code]
              :row-fn spec/map->Comment))


;;; protocol implementation

(defn save-comment [{db :db} {:keys [name text time]} article-code]
  (jdbc/insert! db :comment {:name name
                             :time time
                             :text text
                             :article article-code})
  :ok)

(defn read-comment-counts [this article-codes]
  (select-comment-counts this article-codes))

(defn read-comments [this article-code]
  (select-comments this article-code))
