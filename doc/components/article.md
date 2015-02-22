# Article Component

This component loads and renders articles. From the supplied ArticleDatastore it will retrieve:

* all articles
* articles of a specified month
* one article specified by it's code

The collection of articles, with pagination-info (next and previous page-urls) and general data (recent articles and list of months) is forwarded to the next handler.

## Configuration

Modify your `core.clj` as follows:

```
(def article-handler (map->ArticleHandler {:plugins [:dropbox-plugin
                                                     :smiley-plugin
                                                     :google-map-plugin
                                                     :markdown-plugin]}))

(def system
  (component/system-map
   :web-server (component/using web-server
                                {...
                                 :article-handler :article-handler
                                 ...})
   ...
   :article-handler (component/using article-handler
                                     {:db :article-datastore
                                      :dropbox-plugin    :dropbox-plugin
                                      :smiley-plugin     :smiley-plugin
                                      :google-map-plugin :google-map-plugin
                                      :markdown-plugin   :markdown-plugin})))
```

The `ArticleHandler` expects following options:
* `:plugins`: A vector of plugin keywords, as defined in the system-map.

# Article Management Component

This management components enables the author to compose new articles in the browser. It requires a writable SQL Datastore.

## Configuration

Modify your `core.clj` as follows:

```
(def article-management-handler (map->ArticleManagementHandler {}))

(def system
  (component/system-map
   :web-server (component/using web-server
                                {...
                                 :article-management-handler :article-management-handler
                                 ...})
   ...
   :article-management-handler (component/using article-management-handler
                                                {:db :article-datastore})))
```

# Datastores

The Article Component, as well as the Article Management Component, rely on a datastore.

## File Datastore

The File Datastore is read-only. It reads the articles from a folder in the filesystem.

### Configuration

Modify your `core.clj` as follows:

```
(def article-datastore (map->ArticleFileDatastore {:article-path "articles"
                                                   :articles-per-page 5
                                                   :recent-articles 10}))

(def system
  (component/system-map
   :article-datastore article-datastore
   ...
   :article-handler (component/using article-handler
                                     {:db :article-datastore
                                      ...})
   ...
   :article-management-handler (component/using article-management-handler
                                                {:db :article-datastore})))
```

The `ArticleFileDatastore` expects following options:
* `:article-path`: A path to a folder, which contains article-files in following format: `yyyy-mm-dd-title.md`
* `:articles-per-page`: An integer that specifies how many articles should be displayed per page.
* `:recent-articles`: An integer that specifies how many of the latest articles should be displayed.

## SQL Datastore

The SQL Datastore is read and writable. It was tested with HSQLDB and Postgres.

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

(def article-datastore (map->ArticleSQLDatastore {:db DB_SPEC
                                                  :articles-per-page 5
                                                  :recent-articles 10}))

(def system
  (component/system-map
   :article-datastore article-datastore
   ...
   :article-handler (component/using article-handler
                                     {:db :article-datastore
                                      ...})
   ...
   :article-management-handler (component/using article-management-handler
                                                {:db :article-datastore})))
```

The `ArticleSQLDatastore` expects following options:
* `:db`: A map that defines the connection to a database.
* `:articles-per-page`: As above.
* `:recent-articles`: As above.

