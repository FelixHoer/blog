(ns blog.text-plugin.dropbox
  (:require [blog.text-plugin.plugin :as spec]
            [blog.text-plugin.dropbox-impl :as impl]))

(defrecord DropboxPlugin [user-id]
  spec/Plugin
    (process [this content]
      (impl/process this content)))
