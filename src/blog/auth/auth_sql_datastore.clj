(ns blog.auth.auth-sql-datastore
  (:require [blog.auth.auth-datastore :as spec]
            [blog.auth.auth-sql-datastore-impl :as impl]))

(defrecord AuthSQLDatastore [db]
  spec/AuthDatastore
    (authenticate [this username pwd]
      (impl/authenticate this username pwd))
  spec/AuthManagementDatastore
    (sign-up [this username pwd]
      (impl/sign-up this username pwd))
    (list-users [this]
      (impl/list-users this))
    (confirm-user [this username]
      (impl/confirm-user this username))
    (delete-user [this username]
      (impl/delete-user this username)))
