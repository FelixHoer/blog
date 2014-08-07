(ns blog.article.article-datastore)

(defprotocol ArticleDatastore
  (article [this name])
  (article-page [this page-num])
  (article-month-page [this month page-num]))

(defprotocol ArticleOverviewDatastore
  (article-overview [this]))

(defprotocol ArticleManagementDatastore
  (add-article [this article])
  (edit-article [this code new-body])
  (delete-article [this code]))
