(ns blog.core
  (:use blog.auth.auth-handler
        ;blog.auth.auth-file-datastore
        blog.article.article-handler
        blog.article.article-file-datastore
        ;blog.comment.comment-handler
        ;blog.comment.comment-sql-datastore
        blog.theme.theme-handler
        blog.web-server.web-server
        blog.article-plugin.dropbox
        blog.article-plugin.smiley
        blog.article-plugin.google-map
        blog.article-plugin.markdown)
  (:require [com.stuartsierra.component :as component]))


; plugins

(def DROPBOX_USER_ID "58952800")
(def dropbox-plugin (map->DropboxPlugin {:user-id DROPBOX_USER_ID}))

(def smiley-plugin (map->SmileyPlugin {}))

(def GOOGLE_MAP_APP_KEY "APP_KEY")
(def google-map-plugin (map->GoogleMapPlugin {:app-key GOOGLE_MAP_APP_KEY}))

(def markdown-plugin (map->MarkdownPlugin {}))


; components

(def STATIC_RESOURCE_PATH "static")

(def web-server (map->WebServer {}))

(def auth-handler (map->AuthHandler {:static-resource-path STATIC_RESOURCE_PATH}))

(def ARTICLES_PATH "articles")
(def ARTICLES_PER_PAGE 5)
(def RECENT_ARTICLES 10)
(def article-datastore (map->ArticleFileDatastore {:article-path ARTICLES_PATH
                                                   :articles-per-page ARTICLES_PER_PAGE
                                                   :recent-articles RECENT_ARTICLES}))

(def article-handler (map->ArticleHandler {:plugins [:dropbox-plugin
                                                     :smiley-plugin
                                                     :google-map-plugin
                                                     :markdown-plugin]}))

(def TEMPLATES_RESOURCE_PATH "templates")
(def theme-handler (map->ThemeHandler {:template-resource-path TEMPLATES_RESOURCE_PATH
                                       :static-resource-path STATIC_RESOURCE_PATH}))


; system

(def system
  (component/system-map
   :web-server (component/using web-server
                                {:next :theme-handler})

   :theme-handler (component/using theme-handler
                                   {:next :auth-handler})

   ;:auth-datastore (map->AuthFileDatastore {})
   :auth-handler (component/using auth-handler
                                  {;:db :auth-datastore
                                   :next :article-handler})

   :dropbox-plugin dropbox-plugin
   :smiley-plugin smiley-plugin
   :google-map-plugin google-map-plugin
   :markdown-plugin markdown-plugin
   :article-datastore article-datastore
   :article-handler (component/using article-handler
                                     {:db :article-datastore
                                      :markdown-plugin :markdown-plugin
                                      :dropbox-plugin :dropbox-plugin
                                      :smiley-plugin :smiley-plugin
                                      :google-map-plugin :google-map-plugin
                                      ;:next :comment-handler
                                      })

   ;:comment-datastore (map->CommentSQLDatastore {})
   ;:comment-handler (component/using (map->CommentHandler {})
   ;                                  {:db :comment-datastore
   ;                                   :next :theme-handler})
   ))


; main

(defn -main []
  (component/start system))

;(-main)
