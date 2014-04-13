(ns blog.core
  (:use blog.constants
        blog.auth.auth-handler
        ;blog.auth.auth-file-datastore
        blog.article.article-handler
        blog.article.article-file-datastore
        ;blog.comment.comment-handler
        ;blog.comment.comment-sql-datastore
        blog.theme.theme-handler
        blog.web-server.web-server)
  (:require [com.stuartsierra.component :as component]))


(def system
  (component/system-map
   :web-server (component/using (map->WebServer {})
                                {:next :auth-handler})
   ;:auth-datastore (map->AuthFileDatastore {})
   :auth-handler (component/using (map->AuthHandler {})
                                  {;:db :auth-datastore
                                   :next :article-handler
                                   :final :theme-handler})
   :article-datastore (map->ArticleFileDatastore {})
   :article-handler (component/using (map->ArticleHandler {})
                                     {:db :article-datastore
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
