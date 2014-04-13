(ns blog.theme.template
  (:use blog.constants)
  (:require [clostache.parser :as clostache]))


; templates

(defn template-path [subpath]
  (str TEMPLATES_RESOURCE_PATH "/" subpath ".mustache"))

(defn template [subpath]
  (fn [data]
    (clostache/render-resource (template-path subpath) data)))

(defn templates [& subpaths]
  (let [ts (map template (reverse subpaths))
        iterate-data (fn [data t] (assoc data :body (t data)))]
    (letfn [(generate-template-content
              ([] (generate-template-content {}))
              ([data] (:body (reduce iterate-data data ts))))]
      generate-template-content)))
