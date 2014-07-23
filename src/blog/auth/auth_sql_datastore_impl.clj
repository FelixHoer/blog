(ns blog.auth.auth-sql-datastore-impl
  (:require [clojure.java.jdbc :as jdbc]
            [crypto.password.scrypt :as password]))


;;; helper

(defmacro expect [res form]
  `(try
    (if (= ~res ~form)
      :ok
      :fail)
    (catch Exception e# :fail)))


;;;; setup operations ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn create-auth-table [{db :db}]
  (jdbc/db-do-commands db
    (jdbc/create-table-ddl :auth
                           [:user      "varchar(255)" "UNIQUE"]
                           [:key       "varchar(255)"]
                           [:confirmed "boolean" "NOT NULL" "DEFAULT FALSE"]))
  :ok)

;;; add credentials to db

(defn add-credentials [{db :db} user pwd confirmed?]
  (expect [nil] ; no modified row, just added
          (jdbc/insert! db :auth {:user user
                                  :key (password/encrypt pwd)
                                  :confirmed confirmed?})))


;;;; management operations ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; sign up (done by user)

(defn username-exists? [db user]
  (jdbc/query db [(str "SELECT * "
                       "FROM auth "
                       "WHERE auth.user = ?")
                  user]
              :result-set-fn (comp not empty?)))

(defn sign-up [{db :db :as this} user pwd]
  (if (username-exists? db user)
    :already-taken
    (add-credentials this user pwd false)))


;;; list all users

(defn list-users [{db :db}]
  (jdbc/query db [(str "SELECT auth.user, auth.confirmed "
                       "FROM auth")]))


;;; set an user's status to confirmed

(defn confirm-user [{db :db} user]
  (expect [1] ; one modified row
          (jdbc/update! db
                        :auth
                        {:confirmed true}
                        ["auth.user = ?" user])))


;;; delete an user

(defn delete-user [{db :db} user]
  (expect [1] ; one modified row
          (jdbc/delete! db
                        :auth
                        ["auth.user = ?" user])))


;;;; user operations ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; check credentials / authenticate

(defn select-key [db user]
  (jdbc/query db [(str "SELECT key "
                       "FROM auth "
                       "WHERE auth.confirmed = TRUE "
                       "AND auth.user = ?")
                  user]
              :row-fn :key
              :result-set-fn first))

(defn check-credentials-in-db [db user pwd]
  (if-let [key (select-key db user)]
    (password/check pwd key)))

(defn authenticate [{db :db} user pwd]
  (check-credentials-in-db db user pwd))
