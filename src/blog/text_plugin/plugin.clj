(ns blog.text-plugin.plugin)


(defprotocol Plugin
  (process [this content]))


(defn apply-plugins [body plugins]
  (reduce (fn [current-body plugin] (process plugin current-body))
          body
          plugins))
