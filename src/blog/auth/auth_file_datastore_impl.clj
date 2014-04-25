(ns blog.auth.auth-file-datastore-impl
  (:require [clojure.edn :as edn]
            [crypto.password.scrypt :as password]))


(defn check-credentials [coll user pwd]
  (let [key (get coll user)]
    (password/check pwd key)))

(defn add-credentials [coll user pwd]
  (assoc coll user (password/encrypt pwd)))

(def data (-> {}
              (add-credentials "user" "foobar")
              (add-credentials "user2" "foobar2")))

(defn write-auth-file [coll path]
  (spit path (prn-str coll)))

(defn read-auth-file [path]
  (try
    (edn/read-string (slurp path))
    (catch Exception e
      {})))

(defn add-credentials-to-file [path user pwd]
  (-> (read-auth-file path)
      (add-credentials user pwd)
      (write-auth-file path)))

(defn check-credentials-in-file [path user pwd]
  (-> (read-auth-file path)
      (check-credentials user pwd)))

(defn authenticate-impl [{path :path} user pwd]
  (check-credentials-in-file path user pwd))
