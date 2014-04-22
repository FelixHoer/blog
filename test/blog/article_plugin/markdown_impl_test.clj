(ns blog.article-plugin.markdown-impl-test
  (:use blog.article-plugin.plugin
        blog.article-plugin.markdown)
  (:require [clojure.test :refer :all]
            [blog.article-plugin.markdown-impl :refer :all]))

(def markdown-plugin (map->MarkdownPlugin {}))

(deftest markdown
  (testing "process"
    (is (= (process markdown-plugin "")
           ""))
    (is (= (process markdown-plugin "# Heading")
           "<h1>Heading</h1>"))
    (is (= (process markdown-plugin "text")
           "<p>text</p>"))))
