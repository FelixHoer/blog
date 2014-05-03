(ns blog.comment.comment-sql-datastore-impl
  (:require [clojure.java.jdbc :as jdbc]
            [blog.comment.comment-datastore :as ds]
            [clojure.string :as string]))


; setup operations

(defn create-comment-table [{db :db}]
  (jdbc/db-do-commands db
    (jdbc/create-table-ddl :comment
                           [:name    "varchar(255)"]
                           [:time    "datetime"]
                           [:text    "text"]
                           [:article "varchar(255)"]))
  :ok)


; validation

(def validation-tests [
  [#(-> % :name string/blank?) "Name is blank."]
  [#(-> % :name count (> 255)) "Name is too long."]
  [#(-> % :text string/blank?) "Text is blank."]])

(defn validation-errors [comment]
  (let [results (map (fn [[p e]] (if (p comment) e))
                     validation-tests)]
    (seq (remove nil? results))))


; datastore operations

(defn insert-comment [{db :db} {:keys [name text time]} article-code]
  (jdbc/insert! db :comment {:name name
                             :time time
                             :text text
                             :article article-code})
  :ok)

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

(defn select-comments [{db :db} article-code]
  (jdbc/query db [(str "SELECT name, time, text "
                       "FROM comment "
                       "WHERE article = ? "
                       "ORDER BY time DESC")
                  article-code]
              :row-fn ds/map->Comment))


; component

(defn save-comment-impl [this comment article-code]
  (if-let [errors (validation-errors comment)]
    errors
    (let [result (insert-comment this comment article-code)]
      (if (= :ok result)
        nil
        [result]))))

(defn read-comment-counts-impl [this article-codes]
  (select-comment-counts this article-codes))

(defn read-comments-impl [this article-code]
  (select-comments this article-code))
