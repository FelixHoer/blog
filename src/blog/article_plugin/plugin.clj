(ns blog.article-plugin.plugin)

(defprotocol Plugin
  (process [this content]))
