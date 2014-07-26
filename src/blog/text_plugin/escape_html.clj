(ns blog.text-plugin.escape-html
  (:require [blog.text-plugin.plugin :as spec]
            [blog.text-plugin.escape-html-impl :as impl]))

(defrecord EscapeHTMLPlugin [preserve-ampersand?]
  spec/Plugin
    (process [this content]
      (impl/process this content)))
