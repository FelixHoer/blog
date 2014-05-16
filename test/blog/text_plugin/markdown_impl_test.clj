(ns blog.text-plugin.markdown-impl-test
  (:use blog.text-plugin.plugin
        blog.text-plugin.markdown)
  (:require [clojure.test :refer :all]
            [blog.text-plugin.markdown-impl :refer :all]))

(def markdown-plugin (map->MarkdownPlugin {}))

(deftest markdown
  (testing "process"
    (is (= (process markdown-plugin "")
           ""))
    (is (= (process markdown-plugin "# Heading")
           "<h1>Heading</h1>"))
    (is (= (process markdown-plugin "text")
           "<p>text</p>"))))
