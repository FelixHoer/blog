(ns blog.auth.auth-datastore)

(defprotocol AuthDatastore
  (authenticate [this username password]))

(defprotocol AuthManagementDatastore
  (sign-up [this username password])
  (list-users [this])
  (confirm-user [this username])
  (delete-user [this username]))
