(ns blog.text-plugin.dropbox-impl
  (:require [clojure.string :as string]))

(defn dropbox-url [{user-id :user-id}] (str "http://dl.dropboxusercontent.com/u/" user-id "/"))

(defn process-impl [this content]
  (string/replace content
                  #"\!\[([^\)]*)\]\(db:([^\)]+)\)"
                  (str "![$1](" (dropbox-url this) "$2)")))
