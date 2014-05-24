(ns blog.auth.auth-sql-datastore
  (:use blog.auth.auth-datastore
        [blog.auth.auth-sql-datastore-impl :only [authenticate-impl]]))

(defrecord AuthSQLDatastore [db]
  AuthDatastore
    (authenticate [this username pwd]
      ((var authenticate-impl) this username pwd)))
