(ns blog.comment.comment-handler-impl
  (:require [blog.auth.auth-handler-impl :as helper]
            [blog.comment.comment-datastore :as cdb]
            [blog.handler :as handler]
            [clojure.string :as string]
            [blog.text-plugin.plugin :as plugin]
            [compojure.core :refer [defroutes GET POST]]
            [validateur.validation :as v]))


;;; constants

(def SAVE_FAIL_MSG "Could not save comment!")


;;; helpers

(defn now []
  (java.util.Date.))

(defn comment-name [{:keys [form-params session]}]
  (or (get-in session [:user :username])
      (get form-params "comment-name")
      ""))

(defn comment-text [{params :form-params}]
  (get params "comment-text" ""))

(defn plugin-seq [component]
  (map #(% component) (:plugins component)))

(defn apply-plugins-comments [component comments]
  (let [plugins (plugin-seq component)
        transform-body #(update-in % [:text] plugin/apply-plugins plugins)]
    (map transform-body comments)))


;;; validation

(def new-comment-validator
  (v/validation-set
   (v/presence-of :name)
   (v/length-of   :name :within (range 1 10))
   (v/presence-of :text)))


;;; server endpoints

(defn save-comment [{db :db} article-code req]
  (let [input {:name (comment-name req)
               :text (comment-text req)}
        validate #(let [errors (new-comment-validator input)]
                    (if-not (v/valid? errors)
                      {:comment input
                       :errors errors}))
        process  #(let [comment (cdb/map->Comment (assoc input :time (now)))
                        result (cdb/save-comment db comment article-code)]
                    (if-not (= :ok result)
                      {:comment input
                       :comment-error SAVE_FAIL_MSG}))
        url (str "/articles/" article-code "#comments")
        redirect (helper/local-redirect req url)
        first-flash-data (some #(%) [validate process])]
    (if first-flash-data
      (handler/deep-merge-in redirect [:data :flash]
                             first-flash-data)
      redirect)))

(defn extend-with-comment-counts [{db :db} {{items :items} :data :as resp}]
  (let [article-codes (map :code items)
        count-map (cdb/read-comment-counts db article-codes)
        add-comment-count #(assoc % :comment-count (get count-map (:code %)))
        update-items #(map add-comment-count %)]
    (update-in resp [:data :items] update-items)))

(defn extend-with-comments [{db :db :as this} {{items :items} :data :as resp}]
  (let [[article] items
        comments (cdb/read-comments db (:code article))
        processed-comments (apply-plugins-comments this comments)
        extended-article (assoc article :comments processed-comments)]
    (assoc-in resp [:data :items] [extended-article])))


;;; routes

(defroutes comment-routes

  (POST "/comment/:code" {{code :code} :params component :component :as req}
    (save-comment component code req))

  (GET "*" {component :component resp :resp}
    (case (:template resp)
      :article-list (extend-with-comment-counts component resp)
      :article      (extend-with-comments component resp)
      nil)))


;;; component

(defn handle [this next-handler req]
  (let [extended-req (assoc req :component this)
        resp (comment-routes extended-req)
        next-req (update-in req [:resp] handler/deep-merge resp)]
    (next-handler next-req)))

(defn wrap-handler [this next-handler]
  #(handle this next-handler %))
