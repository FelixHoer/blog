(ns blog.auth.auth-file-datastore
  (:use [blog.auth.auth-datastore :as spec]
        [blog.auth.auth-file-datastore-impl :as impl]]))

(defrecord AuthFileDatastore [path]
  spec/AuthDatastore
    (authenticate [this username pwd]
      (impl/authenticate this username pwd)))
