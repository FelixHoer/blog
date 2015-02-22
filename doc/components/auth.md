# Authentication Component

This component enforces authentication, before the requests are forwared to the following handlers.

## Configuration

Modify your `core.clj` as follows:

```
(def auth-handler (map->AuthHandler {}))

(def system
  (component/system-map
   :web-server (component/using web-server
                                {...
                                 :auth-handler               :auth-handler
                                 ...})
   ...
   :auth-handler (component/using auth-handler
                                  {:db :auth-datastore})))
```

# Authentication Management Component

**TODO**: Write a component that allows new users to register. The administrator then has to manually confirm new users (by setting the `confirmed` flag). This functionality requires a writable SQL Datastore.

# Datastores

The authentication components rely on the datastores to authenticate users and to manage them.

## File Datastore

The information for all users is stored in a file. Passwords are not directly stored, only the result of a key derivation function is kept.

### User Management

To add new users (with password) run `lein repl` and enter the following lines:

```
(use 'blog.auth.auth-file-datastore-impl)
(add-user-to-file auth-datastore "admin" "admin-pw" :admin)
(add-user-to-file auth-datastore "user" "user-pw" :user)
```

After that, commit the file at `AUTH_FILE_PATH` to your cloud hosting provider to apply the changes.

### Configuration

Modify your `core.clj` as follows:

```
(def auth-datastore (map->AuthFileDatastore {:path "users.edn"}))

(def system
  (component/system-map
   :web-server (component/using web-server
                                {...
                                 :auth-handler :auth-handler
                                 ...})
   ...
   :auth-datastore auth-datastore
   :auth-handler (component/using auth-handler
                                  {:db :auth-datastore})))
```

The options for the `AuthFileDatastore` are:
* `:path`: A path that points to the file that contains the users information.

## SQL Datastore

The user information is stored in a SQL database. Currently HSQLDB and Postgres are supported. Passwords are not directly stored, only the result of a key derivation function is kept.

### User Management

To add new users (with password) run `lein repl` and enter the following lines:

```
(use 'blog.auth.auth-sql-datastore-impl)
(add-user auth-datastore {:username "admin"
                          :password "admin-pw"
                          :confirmed true
                          :role :admin})
(add-user auth-datastore {:username "user"
                          :password "user-pw"
                          :confirmed true
                          :role :user})
```

### Configuration

Modify your `core.clj` as follows:

```
(def DB_SPEC {:subprotocol "hsqldb"
              :subname (string/join ";" ["file:/tmp/test-db/blogdb"
                                         "shutdown=true"
                                         "sql.syntax_pgs=true"])
              :user "SA"
              :password ""})

(def DB_SPEC {:subprotocol "postgresql"
              :classname "org.postgresql.Driver"
              :subname (str "//127.0.0.1:5432/blog")
              :user "postgres"
              :password "postgres"})

(def auth-datastore (map->AuthSQLDatastore {:db DB_SPEC}))

(def system
  (component/system-map
   :web-server (component/using web-server
                                {...
                                 :auth-handler :auth-handler
                                 ...})
   ...
   :auth-datastore auth-datastore
   :auth-handler (component/using auth-handler
                                  {:db :auth-datastore})))
```

