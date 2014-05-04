(ns blog.comment.comment-handler-impl
  (:use compojure.core
        blog.handler)
  (:require [blog.auth.auth-handler-impl :as helper]
            [blog.comment.comment-datastore :as cdb]
            [clojure.string :as string]))


(def SAVE_ERROR_MSG "Could not save comment, because: ")

; helpers

(defn now []
  (java.util.Date.))

(defn comment-name [{:keys [params session]}]
  (or (get session :username)
      (get params "comment-name")
      ""))

(defn comment-text [{params :params}]
  (get params "comment-text" ""))


; server endpoints

(defn save-comment [{db :db} article-code req]
  (let [comment (cdb/map->Comment {:name (comment-name req)
                                   :time (now)
                                   :text (comment-text req)})
        errors (cdb/save-comment db comment article-code)]
    (if errors
      (deep-merge (helper/local-redirect req (str "/articles/" article-code))
                  {:data {:flash {:warning (str SAVE_ERROR_MSG
                                                (string/join " " errors))}}})
      (helper/local-redirect req (str "/articles/" article-code "#comments")))))

(defn extend-with-comment-counts [{db :db} {{items :items} :data :as resp}]
  (let [article-codes (map :code items)
        count-map (cdb/read-comment-counts db article-codes)
        add-comment-count #(assoc % :comment-count (get count-map (:code %)))
        update-items #(map add-comment-count %)]
    (update-in resp [:data :items] update-items)))

(defn extend-with-comments [{db :db} {{items :items} :data :as resp}]
  (let [[article] items
        comments (cdb/read-comments db (:code article))
        extended-article (assoc article :comments comments)]
    (assoc-in resp [:data :items] [extended-article])))


; routes

(defroutes comment-routes

  (POST "/comment/:code" {{code :code} :params component :component :as req}
    (save-comment component code req))

  (GET "*" {component :component resp :resp :as req}
    (case (:template resp)
      :article-list (extend-with-comment-counts component resp)
      :article      (extend-with-comments component resp)
      nil)))


; component

(defn start-impl [this]
  this)

(defn stop-impl [this]
  this)

(defn handle-impl [{next :next :as this} req]
  (let [extended-req (assoc req :component this)
        resp (comment-routes extended-req)
        next-req (update-in req [:resp] deep-merge resp)]
    (if next
      (handle next next-req)
      next-req)))
