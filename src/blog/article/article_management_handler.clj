(ns blog.article.article-management-handler
  (:require [blog.handler :as handler]
            [blog.article.article-management-handler-impl :as impl]))

(defrecord ArticleManagementHandler [db]
  handler/Handler
    (wrap-handler [this next-handler]
      (impl/wrap-handler this next-handler)))
