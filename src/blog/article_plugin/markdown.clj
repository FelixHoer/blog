(ns blog.article-plugin.markdown
  (:use blog.article-plugin.plugin
        [blog.article-plugin.markdown-impl :only [process-impl]]))

(defrecord MarkdownPlugin []
  Plugin
    (process [this content] ((var process-impl) this content)))
