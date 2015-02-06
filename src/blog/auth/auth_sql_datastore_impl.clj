(ns blog.auth.auth-sql-datastore-impl
  (:require [clojure.java.jdbc :as jdbc]
            [crypto.password.scrypt :as password]
            [blog.auth.auth-datastore :as spec]))


;;; helpers

(defmacro expect [res form]
  `(try
    (if (= ~res ~form)
      :ok
      :fail)
    (catch Exception e# [:fail e#])))

(defn sql-user->User [m]
  (spec/map->User (update-in m [:role] keyword)))


;;;; setup operations ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn create-auth-table [{db :db}]
  (try
  (jdbc/db-do-commands db
      (jdbc/create-table-ddl :auth
                             [:username  "varchar(255)" "UNIQUE"]
                             [:key       "varchar(255)"]
                             [:confirmed "boolean"      "DEFAULT FALSE"]
                             [:role      "varchar(255)" "DEFAULT 'user'"]))
    :ok
    (catch Exception e :fail)))

;;; add credentials to db

(defn add-user [{db :db} {:keys [username password confirmed role]
                          :or {role :user confirmed false}}]
  (try
    (jdbc/insert! db :auth {:username username
                            :key (password/encrypt password)
                            :confirmed confirmed
                            :role (name role)})
    :ok
    (catch Exception e :fail)))


;;;; management operations ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; sign up (done by user)

(defn username-exists? [db user]
  (jdbc/query db
              [(str "SELECT * "
                    "FROM auth "
                    "WHERE username = ?")
               user]
              :result-set-fn (comp not empty?)))

(defn sign-up [{db :db :as this} user pwd]
  (if (username-exists? db user)
    :already-taken
    (add-user this {:username user
                    :password pwd})))


;;; list all users

(defn list-users [{db :db}]
  (jdbc/query db
              [(str "SELECT username, confirmed, role "
                    "FROM auth")]
              :row-fn sql-user->User))


;;; set an user's status to confirmed

(defn confirm-user [{db :db} user]
  (expect [1] ; one modified row
          (jdbc/update! db
                        :auth
                        {:confirmed true}
                        ["username = ?" user])))


;;; delete an user

(defn delete-user [{db :db} user]
  (expect [1] ; one modified row
          (jdbc/delete! db
                        :auth
                        ["username = ?" user])))


;;;; user operations ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; check credentials / authenticate

(defn select-user [db user]
  (jdbc/query db
              [(str "SELECT username, confirmed, role, key "
                    "FROM auth "
                    "WHERE confirmed = TRUE "
                    "  AND username = ?")
               user]
              :row-fn sql-user->User
              :result-set-fn first))

(defn authenticate [{db :db} username pwd]
  (if-let [user (select-user db username)]
    (if (password/check pwd (:key user))
      (dissoc user :key))))
