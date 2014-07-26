(ns blog.text-plugin.escape-html-impl
  (:require [clojure.string :as string]))

(defn replace-all [content replacements]
  (reduce (fn [accu [from to]] (string/replace accu from to))
          content
          (remove nil? replacements)))

(defn process [{amp? :preserve-ampersand?} content]
  (replace-all content [(if-not amp? ["&" "&amp;"])
                        ["\"" "&quot;"]
                        ["<" "&lt;"]
                        [">" "&gt;"]]))
