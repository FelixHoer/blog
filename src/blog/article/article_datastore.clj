(ns blog.article.article-datastore)

(defprotocol ArticleDatastore
  (article [this name])
  (article-page [this page-num])
  (article-month-page [this month page-num]))
