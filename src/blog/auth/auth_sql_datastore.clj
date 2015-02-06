(ns blog.auth.auth-sql-datastore
  (:require [com.stuartsierra.component :as component]
            [blog.auth.auth-datastore :as spec]
            [blog.auth.auth-sql-datastore-impl :as impl]))

(defrecord AuthSQLDatastore [db]
  component/Lifecycle
    (start [this]
      (impl/create-auth-table this)
      this)
    (stop [this]
      this)

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
