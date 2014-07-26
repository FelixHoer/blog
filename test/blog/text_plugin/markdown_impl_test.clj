(ns blog.text-plugin.markdown-impl-test
  (:require [clojure.test :refer :all]
            [blog.text-plugin.plugin :as p]
            [blog.text-plugin.markdown :as spec]))

(def markdown-plugin (spec/map->MarkdownPlugin {}))

(deftest markdown
  (testing "process"
    (is (= (p/process markdown-plugin "")
           ""))
    (is (= (p/process markdown-plugin "# Heading")
           "<h1>Heading</h1>"))
    (is (= (p/process markdown-plugin "text")
           "<p>text</p>"))))
