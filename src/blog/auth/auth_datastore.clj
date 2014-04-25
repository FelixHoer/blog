(ns blog.auth.auth-datastore)

(defprotocol AuthDatastore
  (authenticate [this username pwd]))
