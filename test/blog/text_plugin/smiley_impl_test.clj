(ns blog.text-plugin.smiley-impl-test
  (:use blog.text-plugin.plugin
        blog.text-plugin.smiley)
  (:require [clojure.test :refer :all]
            [blog.text-plugin.smiley-impl :refer :all]))

(def smiley-plugin (map->SmileyPlugin {}))

(deftest smiley
  (testing "process"
    (is (= (process smiley-plugin "")
           ""))
    (is (= (process smiley-plugin ":)")
           "<img src=\"/smiley/face-smile.png\" />"))
    (is (= (process smiley-plugin ":smile:")
           "<img src=\"/smiley/face-smile.png\" />"))))
