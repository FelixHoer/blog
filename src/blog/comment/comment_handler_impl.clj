(ns blog.comment.comment-handler-impl
  (:use compojure.core
        blog.handler)
  (:require [blog.auth.auth-handler-impl :as helper]
            [blog.comment.comment-datastore :as comment-db]
            [clojure.string :as string]))


; helpers

(defn current-time []
  (java.util.Date.))

(def validate-tests [
  [#(-> % :name string/trim empty?) "Name is empty."]
  [#(-> % :text string/trim empty?) "Text is empty."]])

(defn validate-errors [comment]
  (let [test-results (map (fn [[p e]] (if (p comment) e))
                          validate-tests)]
    (remove nil? test-results)))


; server endpoints

(defn save-comment [{db :db} article-code {params :params :as req}]
  (let [name (or (-> req :session :username)
                 (get params "comment-name" ""))
        text (get params "comment-text" "")
        comment (comment-db/map->Comment {:name name
                                          :time (current-time)
                                          :text text})
        errors (validate-errors comment)]
    (if-not (empty? errors)
      (do
        ; add error messages beyond redirect
        (println errors)
        (helper/local-redirect req (str "/articles/" article-code)))
      (do
        (comment-db/save-comment db comment article-code)
        (helper/local-redirect req (str "/articles/" article-code "#comments"))))))

(defn extend-response-with-comment-counts [{db :db} {{items :items} :data :as resp}]
  (let [article-codes (map :code items)
        count-map (comment-db/read-comment-counts db article-codes)
        add-comment-count #(assoc % :comment-count (get count-map (:code %)))
        update-items #(map add-comment-count %)]
    (update-in resp [:data :items] update-items)))

(defn extend-response-with-comments [{db :db} {{items :items} :data :as resp}]
  (let [article (first items)
        comments (comment-db/read-comments db (:code article))
        extended-article (assoc article :comments comments)]
    (assoc-in resp [:data :items] [extended-article])))


; routes

(defroutes comment-routes

  (POST "/comment/:code" {{code :code} :params component :component :as req}
    (save-comment component code req))

  (GET "*" {component :component resp :resp :as req}
    (case (:template resp)
      :article-list (extend-response-with-comment-counts component resp)
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
