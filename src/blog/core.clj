(ns blog.core
  (:use blog.constants)
  (:use blog.article.article-handler)
  (:use blog.article.article-file-datastore)
  (:use blog.theme.theme-handler)
  (:use blog.web-server.web-server)
  (:require [com.stuartsierra.component :as component]))


(def system
  (component/system-map
   :web-server (component/using (map->WebServer {})
                                ;{:next :authentication}
                                {:next :article-handler})
   ;:authentication-datastore (new-authentication-datastore)
   ;:authentication (component/using (new-authentication)
   ;                                 {:db :authentication-datastore
   ;                                  :next :article-handler
   ;                                  :final :theme-handler})
   :article-datastore (map->ArticleFileDatastore {})
   :article-handler (component/using (map->ArticleHandler {})
                                     {:db :article-datastore
                                      ;:next :comment-handler
                                      :next :theme-handler})
   ;:comment-datastore (new-comment-datastore)
   ;:comment-handler (component/using (new-comment-handler)
   ;                                  {:db :comment-datastore})
   :theme-handler (map->ThemeHandler {})))

(defn -main []
  (component/start system))

(-main)
