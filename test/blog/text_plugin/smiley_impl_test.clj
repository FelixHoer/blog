(ns blog.text-plugin.smiley-impl-test
  (:require [clojure.test :refer :all]
            [blog.text-plugin.plugin :as p]
            [blog.text-plugin.smiley :as spec]))

(def smiley-plugin (spec/map->SmileyPlugin {}))

(deftest smiley
  (testing "process"
    (is (= (p/process smiley-plugin "")
           ""))
    (is (= (p/process smiley-plugin ":)")
           "<img src=\"/smiley/face-smile.png\" />"))
    (is (= (p/process smiley-plugin ":smile:")
           "<img src=\"/smiley/face-smile.png\" />"))))
