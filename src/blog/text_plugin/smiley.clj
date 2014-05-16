(ns blog.text-plugin.smiley
  (:use blog.text-plugin.plugin
        [blog.text-plugin.smiley-impl :only [process-impl]]))

(defrecord SmileyPlugin []
  Plugin
    (process [this content] ((var process-impl) this content)))
