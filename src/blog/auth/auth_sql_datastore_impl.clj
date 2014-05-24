(ns blog.auth.auth-sql-datastore-impl
  (:require [clojure.java.jdbc :as jdbc]
            [crypto.password.scrypt :as password]))


; setup operations

(defn create-auth-table [{db :db}]
  (jdbc/db-do-commands db
    (jdbc/create-table-ddl :auth
                           [:user "varchar(255)"]
                           [:key  "varchar(255)"]))
  :ok)


; add credentials to db

(defn add-credentials [{db :db} user pwd]
  (jdbc/insert! db :auth {:user user
                          :key (password/encrypt pwd)})
  :ok)


; check credentials

(defn select-key [db user]
  (jdbc/query db [(str "SELECT key "
                       "FROM auth "
                       "WHERE auth.user = ?")
                  user]
              :row-fn :key
              :result-set-fn first))

(defn check-credentials-in-db [db user pwd]
  (if-let [key (select-key db user)]
    (password/check pwd key)))


; component interface

(defn authenticate-impl [{db :db} user pwd]
  (check-credentials-in-db db user pwd))
