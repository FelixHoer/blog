(ns blog.article-plugin.markdown-impl
  (:require [markdown.core :as markdown]))

(defn process-impl [this content]
  (markdown/md-to-html-string content))
