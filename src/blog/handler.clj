(ns blog.handler)

(defprotocol Handler
  (handle [this m] "turns an extended-request-map to an extended-response-map"))
