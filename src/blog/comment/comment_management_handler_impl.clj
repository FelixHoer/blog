(ns blog.comment.comment-management-handler-impl
  (:require [blog.auth.auth-handler-impl :as auth]
            [blog.comment.comment-datastore :as ds]
            [blog.handler :as handler]
            [compojure.core :refer [defroutes POST]]))


;;; constants

(def COMMENT_DELETE_SUCCESS_MSG "Deleted comment successfully!")
(def COMMENT_DELETE_FAIL_MSG "Deletion of comment failed!")


;;; endpoints

(defn delete-comment [{:keys [form-params params component] :as req}]
  (let [{comment-id "comment-id"} form-params
        {article-code :code} params
        result (ds/delete-comment (:db component) comment-id)
        flash (if (= :ok result)
                {:info  COMMENT_DELETE_SUCCESS_MSG}
                {:alert COMMENT_DELETE_FAIL_MSG})]
    (handler/deep-merge (auth/local-redirect req (str "/articles/" article-code))
                        {:data {:flash flash}})))


;;; routes

(defroutes comment-mgmt-routes
  (POST "/comment/:code/delete" {session :session :as req}
    (if-not (auth/is-logged-in-as? session :admin)
      (auth/enforce-auth req)
      (delete-comment req))))


;;; component

(defn handle [this next-handler req]
  (let [extended-req (assoc req :component this)
        resp (comment-mgmt-routes extended-req)
        next-req (handler/deep-merge-in req [:resp] resp)]
    (next-handler next-req)))

(defn wrap-handler [this next-handler]
  #(handle this next-handler %))
