(ns blog.comment.comment-handler-impl
  (:use compojure.core
        blog.handler
        ;[blog.article.comment-datastore :only [read-comments]]
        )
  (:require [blog.auth.auth-handler-impl :as helper]))


; server endpoints

(defn save-article [component article-code req]
  (println "save-article")
  (println (-> req :params))
  (helper/local-redirect req (str "/articles/" article-code "#comments")))

(defn extend-response-with-comments [component resp]
  (println "extend-response-with-comments")
  (println (map :code (-> resp :data :items)))
  resp)


; routes

(defroutes comment-routes

  (POST "/comment/:code" {{code :code} :params component :component :as req}
    (save-article component code req))

  (GET "*" {component :component resp :resp :as req}
    (case (:template resp)
      :article-list (extend-response-with-comments component resp)
      :article      (extend-response-with-comments component resp)
      nil)))


; component

(defn start-impl [this]
  this)

(defn stop-impl [this]
  this)

(defn handle-impl [{next :next :as this} req]
  (let [extended-req (assoc req :component this)
        resp (comment-routes extended-req)
        next-req (update-in req [:resp] merge resp)]
    (if next
      (handle next next-req)
      next-req)))
