(ns blog.article-plugin.smiley
  (:use blog.article-plugin.plugin
        [blog.article-plugin.smiley-impl :only [process-impl]]))

(defrecord SmileyPlugin []
  Plugin
    (process [this content] ((var process-impl) this content)))
