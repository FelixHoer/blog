(ns blog.comment.comment-datastore)

(defrecord Comment [name time text])

(defprotocol CommentDatastore
  (save-comment [this comment article-code])
  (read-comment-counts [this article-codes])
  (read-comments [this article-code]))
