(ns blog.comment.comment-handler
  (:require [blog.handler :as handler]
            [blog.comment.comment-handler-impl :as impl]))

(defrecord CommentHandler [db]
  handler/Handler
    (wrap-handler [this next-handler]
      (impl/wrap-handler this next-handler)))
