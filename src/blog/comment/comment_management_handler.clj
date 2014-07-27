(ns blog.comment.comment-management-handler
  (:require [blog.handler :as handler]
            [blog.comment.comment-management-handler-impl :as impl]))

(defrecord CommentManagementHandler [db next]
  handler/Handler
    (wrap-handler [this next-handler]
      (impl/wrap-handler this next-handler)))
