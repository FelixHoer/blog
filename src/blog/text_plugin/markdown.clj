(ns blog.text-plugin.markdown
  (:require [blog.text-plugin.plugin :as spec]
            [blog.text-plugin.markdown-impl :as impl]))

(defrecord MarkdownPlugin []
  spec/Plugin
    (process [this content]
      (impl/process this content)))
