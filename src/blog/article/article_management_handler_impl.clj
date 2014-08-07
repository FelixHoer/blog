(ns blog.article.article-management-handler-impl
  (:require [blog.auth.auth-handler-impl :as auth]
            [blog.article.article-datastore :as ds]
            [blog.article.helpers :as helpers]
            [blog.handler :as handler]
            [compojure.core :refer [defroutes GET POST]]
            [validateur.validation :as v]))


;;; constants

(def SAVE_SUCCESS_MSG "Saved article successfully!")
(def SAVE_FAIL_MSG "Failed to save article!")

(def EDIT_DOES_NOT_EXIST "Specified article does not exist!")
(def EDIT_SUCCESS_MSG "Edited article successfully!")
(def EDIT_FAIL_MSG "Failed to edit article!")

(def DELETE_SUCCESS_MSG "Deleted article successfully!")
(def DELETE_FAIL_MSG "Deletion of article failed!")


;;; helpers

(defn date-format []
  (doto (java.text.SimpleDateFormat. "yyyy-MM-dd")
    (.setLenient false)))

(defn now []
  (java.util.Date.))

(defn now-string []
  (.format (date-format) (now)))


;;; validation

(defn is-date? [d]
  (try
    (.parse (date-format) d)
    true
    (catch java.text.ParseException e
      false)))

(def new-article-validator
  (v/validation-set
   (v/presence-of :title)
   (v/validate-when #(contains? % :title)
     (v/length-of :title :within (range 1 100)))

   (v/presence-of :date)
   (v/validate-when #(contains? % :date)
     (v/format-of   :date :format #"\d\d\d\d-\d\d-\d\d"))
   (v/validate-when #(contains? % :date)
     (v/validate-by :date is-date?
                    :message "is not a valid date"))

   (v/presence-of :body)))

(def edit-article-validator
  (v/validation-set
   (v/presence-of :body)))


;;; endpoints

;; compose + save

(defn show-compose-form [req & [data]]
  {:template :article-compose
   :data (handler/deep-merge {:article {:date (now-string)}}
                             data)})

(defn save-new-article [{{t "title" d "date" b "body"} :form-params
                         {db :db} :component
                         :as req}]
  (let [input {:title t :date d :body b}
        validate #(let [errors (new-article-validator input)]
                    (if-not (v/valid? errors)
                      (show-compose-form req {:article input
                                              :errors errors})))
        process  #(let [article (helpers/complete-article input)
                        result (ds/add-article db article)]
                    (if-not (= :ok result)
                      (show-compose-form req {:article input
                                              :flash {:info SAVE_FAIL_MSG}})))
        redirect #(let [{code :code} (helpers/complete-article input)
                        url (str "/articles/" code)]
                    (handler/deep-merge (auth/local-redirect req url)
                                        {:data {:flash {:info SAVE_SUCCESS_MSG}}}))]
    (some #(%) [validate process redirect])))

;; edit

(defn show-edit-form [{{code :code} :params
                       {db :db} :component
                       :as req}
                      & [data]]
  (let [{[article] :items} (ds/article db code)]
    (if article
      {:template :article-edit
       :data (handler/deep-merge {:article article}
                                 data)}
      (handler/deep-merge (auth/local-redirect req (str "/articles"))
                          {:data {:flash {:warning EDIT_DOES_NOT_EXIST}}}))))

(defn edit-article [{{code :code} :params
                     {body "body"} :form-params
                     {db :db} :component
                     :as req}]
  (let [input {:body body}
        validate #(let [errors (edit-article-validator input)]
                    (if-not (v/valid? errors)
                      (show-edit-form req {:article input
                                           :errors errors})))
        process  #(let [result (ds/edit-article db code body)]
                    (if-not (= :ok result)
                      (show-edit-form req {:article input
                                           :flash {:info EDIT_FAIL_MSG}})))
        redirect #(let [url (str "/articles/" code)]
                    (handler/deep-merge (auth/local-redirect req url)
                                        {:data {:flash {:info EDIT_SUCCESS_MSG}}}))]
    (some #(%) [validate process redirect])))

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
