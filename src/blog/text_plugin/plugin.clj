(ns blog.text-plugin.plugin)

(defprotocol Plugin
  (process [this content]))
