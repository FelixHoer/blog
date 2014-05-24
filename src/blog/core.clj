(ns blog.core
  (:use blog.auth.auth-handler
        blog.auth.auth-file-datastore
        blog.auth.auth-sql-datastore
        blog.article.article-handler
        blog.article.article-file-datastore
        blog.comment.comment-handler
        blog.comment.comment-sql-datastore
        blog.theme.theme-handler
        blog.web-server.web-server
        blog.text-plugin.escape-html
        blog.text-plugin.dropbox
        blog.text-plugin.smiley
        blog.text-plugin.google-map
        blog.text-plugin.markdown)
  (:require [com.stuartsierra.component :as component]
            [clojure.string :as string]))


; plugins

(def dropbox-plugin (map->DropboxPlugin {:user-id "DROPBOX_USER_ID"}))

(def smiley-plugin (map->SmileyPlugin {}))

(def google-map-plugin (map->GoogleMapPlugin {:app-key "GOOGLE_MAP_APP_KEY"}))

(def markdown-plugin (map->MarkdownPlugin {}))

(def escape-html-plugin (map->EscapeHTMLPlugin {:preserve-ampersand? true}))


; components

(def CSP {:default-src ["'self'"]
          :img-src     ["*"]
          :script-src  ["https://ajax.googleapis.com"
                        "https://oss.maxcdn.com"
                        "https://netdna.bootstrapcdn.com"]
          :style-src   ["'self'"
                        "https://netdna.bootstrapcdn.com"]
          :font-src    ["https://netdna.bootstrapcdn.com"]
          :frame-src   ["https://www.google.com/maps/embed/"]})

(def web-server (map->WebServer {:port 8080
                                 :csp CSP}))

#_(def web-server (map->WebServer {:port 8080
                                   :ssl {:via-reverse-proxy? true}
                                   :csp CSP}))

#_(def web-server (map->WebServer {:port 8080
                                   :ssl {:keystore "/tmp/keystore"
                                         :key-password "jettypass"
                                         :ssl-port 8443}
                                   :csp CSP}))

(def theme-handler (map->ThemeHandler {:template-resource-path "templates"
                                       :static-resource-path "static"}))


(def DB_SPEC {:subprotocol "hsqldb"
              :subname (string/join ";" ["file:/tmp/test-db/blogdb"
                                         "shutdown=true"
                                         "sql.syntax_pgs=true"])
              :user "SA"
              :password ""})

(def auth-datastore (map->AuthSQLDatastore {:db DB_SPEC}))
#_(def auth-datastore (map->AuthFileDatastore {:path "users.edn"}))

(def auth-handler (map->AuthHandler {}))

(def article-datastore (map->ArticleFileDatastore {:article-path "articles"
                                                   :articles-per-page 5
                                                   :recent-articles 10}))

(def article-handler (map->ArticleHandler {:plugins [:dropbox-plugin
                                                     :smiley-plugin
                                                     :google-map-plugin
                                                     :markdown-plugin]}))

(def comment-datastore (map->CommentSQLDatastore {:db DB_SPEC}))

(def comment-handler (map->CommentHandler {:plugins [:escape-html-plugin
                                                     :smiley-plugin
                                                     :markdown-plugin]}))


; system

(def system
  (component/system-map
   :web-server (component/using web-server
                                {:next :theme-handler})

   :theme-handler (component/using theme-handler
                                   {:next :auth-handler})

   :auth-datastore auth-datastore
   :auth-handler (component/using auth-handler
                                  {:db :auth-datastore
                                   :next :article-handler})

   :escape-html-plugin escape-html-plugin
   :dropbox-plugin dropbox-plugin
   :smiley-plugin smiley-plugin
   :google-map-plugin google-map-plugin
   :markdown-plugin markdown-plugin

   :article-datastore article-datastore
   :article-handler (component/using article-handler
                                     {:db :article-datastore
                                      :dropbox-plugin :dropbox-plugin
                                      :smiley-plugin :smiley-plugin
                                      :google-map-plugin :google-map-plugin
                                      :markdown-plugin :markdown-plugin
                                      :next :comment-handler})

   :comment-datastore comment-datastore
   :comment-handler (component/using comment-handler
                                     {:db :comment-datastore
                                      :escape-html-plugin :escape-html-plugin
                                      :smiley-plugin :smiley-plugin
                                      :markdown-plugin :markdown-plugin})))


; main

(defn -main []
  (component/start system))

(defn restart []
  (alter-var-root #'system component/stop)
  (alter-var-root #'system component/start))

;(alter-var-root #'system component/start)
;(alter-var-root #'system component/stop)
;(restart)
