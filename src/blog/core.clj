(ns blog.core
  (:use blog.constants)
  (:use blog.content.core)
  (:use blog.content.data)
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
   :article-datastore (new-article-file-datastore)
   :article-handler (component/using (new-article-handler)
                                     {:db :article-datastore
                                      ;:next :comment-handler
                                      :next :theme-handler})
   ;:comment-datastore (new-comment-datastore)
   ;:comment-handler (component/using (new-comment-handler)
   ;                                  {:db :comment-datastore})
   :theme-handler (map->ThemeHandler {})))

(defn -main []
  (component/start system))

;(-main)
