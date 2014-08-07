(ns blog.article.article-file-datastore
  (:require [blog.article.article-datastore :as spec]
            [blog.article.article-file-datastore-impl :as impl]))

(defrecord ArticleFileDatastore []
  spec/ArticleDatastore
    (article [this code]
      (impl/article this code))
    (article-page [this page-num]
      (impl/article-page this page-num))
    (article-month-page [this month page-num]
      (impl/article-month-page this month page-num))

  spec/ArticleOverviewDatastore
    (article-overview [this]
      (impl/article-overview this)))
