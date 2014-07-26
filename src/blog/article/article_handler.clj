(ns blog.article.article-handler
  (:require [blog.handler :as handler]
            [blog.article.article-handler-impl :as impl]))

(defrecord ArticleHandler [db]
  handler/Handler
    (wrap-handler [this next-handler]
      (impl/wrap-handler this next-handler)))
