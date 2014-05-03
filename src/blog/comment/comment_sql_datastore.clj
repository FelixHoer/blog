(ns blog.comment.comment-sql-datastore
  (:use blog.comment.comment-datastore
        [blog.comment.comment-sql-datastore-impl
         :only [save-comment-impl read-comments-impl read-comment-counts-impl]]))

(defrecord CommentSQLDatastore [db]
  CommentDatastore
    (save-comment [this comment article-code]
      ((var save-comment-impl) this comment article-code))
    (read-comment-counts [this article-codes]
      ((var read-comment-counts-impl) this article-codes))
    (read-comments [this article-code]
      ((var read-comments-impl) this article-code)))
