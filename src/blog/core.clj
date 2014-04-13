(ns blog.core
  (:use blog.constants
        blog.auth.auth-handler
        ;blog.auth.auth-file-datastore
        blog.article.article-handler
        blog.article.article-file-datastore
        ;blog.comment.comment-handler
        ;blog.comment.comment-sql-datastore
        blog.theme.theme-handler
        blog.web-server.web-server
        blog.article-plugin.dropbox
        blog.article-plugin.markdown)
  (:require [com.stuartsierra.component :as component]))


(def DROPBOX_USER_ID "58952800")
(def dropbox-plugin (map->DropboxPlugin {:user-id DROPBOX_USER_ID}))

(def markdown-plugin (map->MarkdownPlugin {}))

(def system
  (component/system-map
   :web-server (component/using (map->WebServer {})
                                {:next :auth-handler})
   ;:auth-datastore (map->AuthFileDatastore {})
   :auth-handler (component/using (map->AuthHandler {})
                                  {;:db :auth-datastore
                                   :next :article-handler
                                   :final :theme-handler})
   :dropbox-plugin dropbox-plugin
   :markdown-plugin markdown-plugin
   :article-datastore (map->ArticleFileDatastore {})
   :article-handler (component/using (map->ArticleHandler {:plugins [:dropbox-plugin :markdown-plugin]})
                                     {:db :article-datastore
                                      :markdown-plugin :markdown-plugin
                                      :dropbox-plugin :dropbox-plugin
                                      ;:next :comment-handler
                                      :next :theme-handler})
   ;:comment-datastore (map->CommentSQLDatastore {})
   ;:comment-handler (component/using (map->CommentHandler {})
   ;                                  {:db :comment-datastore
   ;                                   :next :theme-handler})
   :theme-handler (map->ThemeHandler {})))

(defn -main []
  (component/start system))

;(-main)
