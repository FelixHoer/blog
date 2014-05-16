(ns blog.text-plugin.markdown
  (:use blog.text-plugin.plugin
        [blog.text-plugin.markdown-impl :only [process-impl]]))

(defrecord MarkdownPlugin []
  Plugin
    (process [this content] ((var process-impl) this content)))
