(ns blog.article.article-management-handler-impl
  (:require [blog.auth.auth-handler-impl :as auth]
            [blog.article.article-datastore :as ds]
            [blog.article.helpers :as helpers]
            [blog.handler :as handler]
            [compojure.core :refer [defroutes GET POST]]))


;;; constants

(def SAVE_SUCCESS_MSG "Saved article successfully!")
(def SAVE_FAIL_MSG "Failed to save article!")

(def EDIT_DOES_NOT_EXIST "Specified article does not exist!")
(def EDIT_SUCCESS_MSG "Edited article successfully!")
(def EDIT_FAIL_MSG "Failed to edit article!")

(def DELETE_SUCCESS_MSG "Deleted article successfully!")
(def DELETE_FAIL_MSG "Deletion of article failed!")


;;; helpers

(defn now []
  (java.util.Date.))


;;; endpoints

;; compose + save

(defn show-compose-form [req]
  {:template :article-compose
   :data {:article {:date (now)}}})

(defn save-new-article [{{n "name" d "date" b "body"} :form-params
                         {db :db} :component
                         :as req}]
  ; TODO validate
  (let [code (-> (helpers/parse-article-code d)
                 (assoc :title n)
                 (helpers/build-article-code))
        article {:name code :date d :body b}
        result (ds/add-article db article)
        {:keys [url flash]} (if (= :ok result)
                              {:url (str "/articles/" code)
                               :flash {:info  SAVE_SUCCESS_MSG}}
                              {:url "/articles/compose"
                               :flash {:alert SAVE_FAIL_MSG}})]
    (handler/deep-merge (auth/local-redirect req url)
                        {:data {:flash flash}})))

;; edit

(defn show-edit-form [{{code :code} :params {db :db} :component :as req}]
  (let [{[article] :items} (ds/article db code)]
    (if article
      {:template :article-edit
       :data {:article article}}
      (handler/deep-merge (auth/local-redirect req (str "/articles"))
                          {:data {:flash {:warning EDIT_DOES_NOT_EXIST}}}))))

(defn edit-article [{{code :code} :params
                     {body "body"} :form-params
                     {db :db} :component
                     :as req}]
  ; TODO validate
  (let [result (ds/edit-article db code body)
        {:keys [url flash]} (if (= :ok result)
                              {:url (str "/articles/" code)
                               :flash {:info  EDIT_SUCCESS_MSG}}
                              {:url (str "/articles/" code "/edit")
                               :flash {:alert EDIT_FAIL_MSG}})]
    (handler/deep-merge (auth/local-redirect req url)
                        {:data {:flash flash}})))

;; delete

(defn delete-article [{{code :code} :params {db :db} :component :as req}]
  (let [result (ds/delete-article db code)
        {:keys [url flash]} (if (= :ok result)
                              {:url "/articles/"
                               :flash {:info  DELETE_SUCCESS_MSG}}
                              {:url (str "/articles/" code)
                               :flash {:alert DELETE_FAIL_MSG}})]
    (handler/deep-merge (auth/local-redirect req (str "/articles"))
                        {:data {:flash flash}})))


;;; routes

(defroutes article-mgmt-routes
  (GET "/articles/compose" {session :session :as req}
    (if-not (auth/is-logged-in-as? session :admin)
      (auth/enforce-auth req)
      (show-compose-form req)))
  (POST "/articles/compose" {session :session :as req}
    (if-not (auth/is-logged-in-as? session :admin)
      (auth/enforce-auth req)
      (save-new-article req)))

  (GET "/articles/:code/edit" {session :session :as req}
    (if-not (auth/is-logged-in-as? session :admin)
      (auth/enforce-auth req)
      (show-edit-form req)))
  (POST "/articles/:code/edit" {session :session :as req}
    (if-not (auth/is-logged-in-as? session :admin)
      (auth/enforce-auth req)
      (edit-article req)))

  (POST "/articles/:code/delete" {session :session :as req}
    (if-not (auth/is-logged-in-as? session :admin)
      (auth/enforce-auth req)
      (delete-article req))))


;;; component

(defn handle [this next-handler req]
  (let [extended-req (assoc req :component this)]
    (if-let [resp (article-mgmt-routes extended-req)]
      (handler/deep-merge (:resp req) resp)
      (next-handler req))))

(defn wrap-handler [this next-handler]
  #(handle this next-handler %))
