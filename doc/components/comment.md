# Comment Component

The comment component embeds comments into the articles. It also provides functionality to post new comments.

## Configuration

Modify your `core.clj` as follows:

```
(def comment-handler (map->CommentHandler {:plugins [:escape-html-plugin
                                                     :smiley-plugin
                                                     :markdown-plugin]}))

(def system
  (component/system-map
   :web-server (component/using web-server
                                {...
                                 :comment-handler :comment-handler})
   ...
   :comment-handler (component/using comment-handler
                                     {:db :comment-datastore
                                      :escape-html-plugin :escape-html-plugin
                                      :smiley-plugin      :smiley-plugin
                                      :markdown-plugin    :markdown-plugin})))
```

The `CommentHandler` expects following options:
* `:plugins`: A vector of keywords from the system-map. This vector defines which plugins should be performed on the comment text.

# Comment Management Component

The comment management component provides a way for the administrator to delete inappropriate comments.

## Configuration

Modify your `core.clj` as follows:

```
(def comment-management-handler (map->CommentManagementHandler {}))

(def system
  (component/system-map
   :web-server (component/using web-server
                                {...
                                 :comment-management-handler :comment-management-handler})
   ...
   :comment-management-handler (component/using comment-management-handler
                                                {:db :comment-datastore})))
```

# Datastores

## SQL Datastore

The comments are stored in a SQL database. Currently HSQLDB and Postgres are supported.

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

(def comment-datastore (map->CommentSQLDatastore {:db DB_SPEC}))

(def system
  (component/system-map
   ...
   :comment-datastore comment-datastore
   :comment-handler (component/using comment-handler
                                     {:db :comment-datastore
                                      ...})))
   :comment-management-handler (component/using comment-management-handler
                                                {:db :comment-datastore})))
```
