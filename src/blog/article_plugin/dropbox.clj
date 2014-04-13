(ns blog.article-plugin.dropbox
  (:use blog.article-plugin.plugin
        [blog.article-plugin.dropbox-impl :only [process-impl]]))

(defrecord DropboxPlugin [user-id]
  Plugin
    (process [this content] ((var process-impl) this content)))
