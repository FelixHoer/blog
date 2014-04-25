(ns blog.auth.auth-file-datastore
  (:use blog.auth.auth-datastore
        [blog.auth.auth-file-datastore-impl :only [authenticate-impl]]))

(defrecord AuthFileDatastore [path]
  AuthDatastore
    (authenticate [this username pwd]
      ((var authenticate-impl) this username pwd)))
