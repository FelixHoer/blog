(ns blog.text-plugin.google-map
  (:require [blog.text-plugin.plugin :as spec]
            [blog.text-plugin.google-map-impl :as impl]))

(defrecord GoogleMapPlugin [app-key]
  spec/Plugin
    (process [this content]
      (impl/process this content)))
