(ns blog.auth.auth-datastore)

(defrecord User [username password confirmed role])

(defprotocol AuthDatastore
  (authenticate [this username password]))

(defprotocol AuthManagementDatastore
  (sign-up [this username password])
  (list-users [this])
  (confirm-user [this username])
  (delete-user [this username]))
