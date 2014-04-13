(ns blog.handler)

(defprotocol Handler
  (handle [this m]))
