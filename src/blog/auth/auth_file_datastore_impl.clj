(ns blog.auth.auth-file-datastore-impl
  (:require [clojure.edn :as edn]
            [crypto.password.scrypt :as password]
            [blog.auth.auth-datastore :as spec]))


;;; authenticate / get users

(defn read-auth-file [path]
  (try
    (edn/read-string (slurp path))
    (catch Exception e
      nil)))

(defn file-user->User [username role]
  (spec/map->User {:username username
                   :role role
                   :confirmed true}))

(defn get-checked-user [user-map username pwd]
  (if-let [{key :key role :role} (get user-map username)]
    (if (password/check pwd key)
      (file-user->User username role))))

(defn authenticate [{path :path} user pwd]
  (-> (read-auth-file path)
      (get-checked-user user pwd)))


;;; add new users

(defn add-user [coll user pwd role]
  (assoc coll user {:role role
                    :key (password/encrypt pwd)}))

(defn write-auth-file [coll path]
  (spit path (prn-str coll)))

(defn add-user-to-file [{path :path} user pwd role]
  (-> (read-auth-file path)
      (add-user user pwd role)
      (write-auth-file path)))
