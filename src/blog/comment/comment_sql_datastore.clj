(ns blog.comment.comment-sql-datastore
  (:require [blog.comment.comment-datastore :as spec]
            [blog.comment.comment-sql-datastore-impl :as impl]))

(defrecord CommentSQLDatastore [db]
  spec/CommentDatastore
    (save-comment [this comment article-code]
      (impl/save-comment this comment article-code))
    (read-comment-counts [this article-codes]
      (impl/read-comment-counts this article-codes))
    (read-comments [this article-code]
      (impl/read-comments this article-code))
  spec/CommentManagementDatastore
    (delete-comment [this comment-id]
      (impl/delete-comment this comment-id)))
