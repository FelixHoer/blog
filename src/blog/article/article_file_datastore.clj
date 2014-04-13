(ns blog.article.article-file-datastore
  (:use blog.article.article-datastore
        [blog.article.article-file-datastore-impl
         :only [article-impl article-page-impl article-month-page-impl]]))

(defrecord ArticleFileDatastore []
  ArticleDatastore
    (article [this code]
      ((var article-impl) this code))
    (article-page [this page-num]
      ((var article-page-impl) this page-num))
    (article-month-page [this month page-num]
      ((var article-month-page-impl) this month page-num)))
