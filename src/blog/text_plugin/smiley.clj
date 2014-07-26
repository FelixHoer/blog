(ns blog.text-plugin.smiley
  (:require [blog.text-plugin.plugin :as spec]
            [blog.text-plugin.smiley-impl :as impl]))

(defrecord SmileyPlugin []
  spec/Plugin
    (process [this content]
      (impl/process this content)))
