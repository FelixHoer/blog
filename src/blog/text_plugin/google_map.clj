(ns blog.text-plugin.google-map
  (:use blog.text-plugin.plugin
        [blog.text-plugin.google-map-impl :only [process-impl]]))

(defrecord GoogleMapPlugin [app-key]
  Plugin
    (process [this content] ((var process-impl) this content)))
