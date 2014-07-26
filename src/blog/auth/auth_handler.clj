(ns blog.auth.auth-handler
  (:require [blog.handler :as handler]
            [blog.auth.auth-handler-impl :as impl]))

(defrecord AuthHandler [db]
  handler/Handler
    (wrap-handler [this next-handler]
      (impl/wrap-handler this next-handler)))
