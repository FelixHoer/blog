(ns blog.text-plugin.markdown-impl
  (:require [markdown.core :as markdown]))

(defn process [this content]
  (markdown/md-to-html-string content))
