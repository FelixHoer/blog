(ns blog.text-plugin.dropbox-impl
  (:require [clojure.string :as string]))

(defn dropbox-url [{user-id :user-id}]
  (str "https://dl.dropboxusercontent.com/u/" user-id "/"))

(defn process [this content]
  (string/replace content
                  #"\!\[([^\)]*)\]\(db:([^\)]+)\)"
                  (str "![$1](" (dropbox-url this) "$2)")))
