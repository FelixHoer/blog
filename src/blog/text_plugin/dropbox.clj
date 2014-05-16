(ns blog.text-plugin.dropbox
  (:use blog.text-plugin.plugin
        [blog.text-plugin.dropbox-impl :only [process-impl]]))

(defrecord DropboxPlugin [user-id]
  Plugin
    (process [this content] ((var process-impl) this content)))
