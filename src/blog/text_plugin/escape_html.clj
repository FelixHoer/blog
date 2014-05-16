(ns blog.text-plugin.escape-html
  (:use blog.text-plugin.plugin
        [blog.text-plugin.escape-html-impl :only [process-impl]]))

(defrecord EscapeHTMLPlugin [preserve-ampersand?]
  Plugin
    (process [this content] ((var process-impl) this content)))
