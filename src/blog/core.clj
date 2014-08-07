(ns blog.core
  (:require [com.stuartsierra.component :as component]
            [clojure.string :as string]
            ;; auth
            [blog.auth.auth-handler                  :refer [map->AuthHandler]]
            [blog.auth.auth-file-datastore           :refer [map->AuthFileDatastore]]
            [blog.auth.auth-sql-datastore            :refer [map->AuthSQLDatastore]]
            ;; article
            [blog.article.article-handler            :refer [map->ArticleHandler]]
            [blog.article.article-management-handler :refer [map->ArticleManagementHandler]]
            ;[blog.article.article-file-datastore     :refer [map->ArticleFileDatastore]]
            [blog.article.article-sql-datastore      :refer [map->ArticleSQLDatastore]]
            ;; comment
            [blog.comment.comment-handler            :refer [map->CommentHandler]]
            [blog.comment.comment-management-handler :refer [map->CommentManagementHandler]]
            [blog.comment.comment-sql-datastore      :refer [map->CommentSQLDatastore]]
            ;; theme
            [blog.theme.theme-handler                :refer [map->ThemeHandler]]
            ;; web server
            [blog.web-server.web-server              :refer [map->WebServer]]
            ;; plugins
            [blog.text-plugin.escape-html            :refer [map->EscapeHTMLPlugin]]
            [blog.text-plugin.dropbox                :refer [map->DropboxPlugin]]
            [blog.text-plugin.smiley                 :refer [map->SmileyPlugin]]
            [blog.text-plugin.google-map             :refer [map->GoogleMapPlugin]]
            [blog.text-plugin.markdown               :refer [map->MarkdownPlugin]]))


;;; plugins

(def dropbox-plugin     (map->DropboxPlugin {:user-id "DROPBOX_USER_ID"}))
(def smiley-plugin      (map->SmileyPlugin {}))
(def google-map-plugin  (map->GoogleMapPlugin {:app-key "GOOGLE_MAP_APP_KEY"}))
(def markdown-plugin    (map->MarkdownPlugin {}))
(def escape-html-plugin (map->EscapeHTMLPlugin {:preserve-ampersand? true}))


;;; components

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
                                 :csp CSP
                                 :handlers [:theme-handler
                                            :auth-handler
                                            :article-management-handler
                                            :article-handler
                                            :comment-management-handler
                                            :comment-handler]}))

#_(def web-server (map->WebServer {:port 8080
                                   :ssl {:via-reverse-proxy? true}
                                   :csp CSP
                                   :handlers [:theme-handler
                                              :auth-handler
                                              :article-handler
                                              :comment-handler]}))

#_(def web-server (map->WebServer {:port 8080
                                   :ssl {:keystore "/tmp/keystore"
                                         :key-password "jettypass"
                                         :ssl-port 8443}
                                   :csp CSP
                                   :handlers [:theme-handler
                                              :auth-handler
                                              :article-handler
                                              :comment-handler]}))

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

(def article-datastore (map->ArticleSQLDatastore {:db DB_SPEC
                                                  :articles-per-page 5
                                                  :recent-articles 10}))

#_(def article-datastore (map->ArticleFileDatastore {:article-path "articles"
                                                     :articles-per-page 5
                                                     :recent-articles 10}))

(def article-handler (map->ArticleHandler {:plugins [:dropbox-plugin
                                                     :smiley-plugin
                                                     :google-map-plugin
                                                     :markdown-plugin]}))

(def article-management-handler (map->ArticleManagementHandler {}))

(def comment-datastore (map->CommentSQLDatastore {:db DB_SPEC}))

(def comment-handler (map->CommentHandler {:plugins [:escape-html-plugin
                                                     :smiley-plugin
                                                     :markdown-plugin]}))

(def comment-management-handler (map->CommentManagementHandler {}))


;;; system

(def system
  (component/system-map
   :web-server (component/using web-server
                                {:theme-handler              :theme-handler
                                 :auth-handler               :auth-handler
                                 :article-management-handler :article-management-handler
                                 :article-handler            :article-handler
                                 :comment-management-handler :comment-management-handler
                                 :comment-handler            :comment-handler})

   :theme-handler theme-handler

   :auth-datastore auth-datastore
   :auth-handler (component/using auth-handler
                                  {:db :auth-datastore})

   :escape-html-plugin escape-html-plugin
   :dropbox-plugin     dropbox-plugin
   :smiley-plugin      smiley-plugin
   :google-map-plugin  google-map-plugin
   :markdown-plugin    markdown-plugin

   :article-datastore article-datastore
   :article-handler (component/using article-handler
                                     {:db :article-datastore
                                      :dropbox-plugin    :dropbox-plugin
                                      :smiley-plugin     :smiley-plugin
                                      :google-map-plugin :google-map-plugin
                                      :markdown-plugin   :markdown-plugin})
   :article-management-handler (component/using article-management-handler
                                                {:db :article-datastore})

   :comment-datastore comment-datastore
   :comment-handler (component/using comment-handler
                                     {:db :comment-datastore
                                      :escape-html-plugin :escape-html-plugin
                                      :smiley-plugin      :smiley-plugin
                                      :markdown-plugin    :markdown-plugin})
   :comment-management-handler (component/using comment-management-handler
                                                {:db :comment-datastore})))


;;; main

(defn start []
  (alter-var-root #'system component/start))

(defn stop []
  (alter-var-root #'system component/stop))

(defn restart []
  (stop)
  (start))

(defn -main []
  (start))

(selmer.parser/cache-off!)
;(start)
;(stop)
;(restart)
