(ns blog.theme.template
  (:require [clostache.parser :as clostache]))


; templates

(defn template-path [base-path sub-path]
  (str base-path "/" sub-path ".mustache"))

(defn template [base-path sub-path]
  (fn [data]
    (clostache/render-resource (template-path base-path sub-path) data)))

(defn templates [base-path sub-paths]
  (let [ts (map #(template base-path %) (reverse sub-paths))
        iterate-data (fn [data t] (assoc data :body (t data)))]
    (letfn [(generate-template-content
              ([] (generate-template-content {}))
              ([data] (:body (reduce iterate-data data ts))))]
      generate-template-content)))
