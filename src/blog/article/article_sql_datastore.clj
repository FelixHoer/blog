(ns blog.article.article-sql-datastore
  (:require [blog.article.article-datastore :as spec]
            [blog.article.article-sql-datastore-impl :as impl]))

(defrecord ArticleSQLDatastore []
  spec/ArticleDatastore
    (article [this code]
      (impl/article-impl this code))
    (article-page [this page-num]
      (impl/article-page-impl this page-num))
    (article-month-page [this month page-num]
      (impl/article-month-page-impl this month page-num))
  spec/ArticleManagementDatastore
    (add-article [this article]
      (impl/add-article this article))
    (edit-article [this code new-body]
      (impl/edit-article this code new-body))
    (delete-article [this code]
      (impl/delete-article this code)))
