(ns blog.article-plugin.google-map
  (:use blog.article-plugin.plugin
        [blog.article-plugin.google-map-impl :only [process-impl]]))

(defrecord GoogleMapPlugin [app-key]
  Plugin
    (process [this content] ((var process-impl) this content)))
