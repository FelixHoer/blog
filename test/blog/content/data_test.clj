(ns blog.content.data-test
  (:require [clojure.test :refer :all]
            [blog.content.data :refer :all]))

(def test-articles
  ["2014-04-02-first-post.md"
   "2014-05-01-next-post.md"])

(deftest data
  (testing "group-by-month"
    (is (= (group-by-month test-articles)
           {"2014-05" ["2014-05-01-next-post.md"],
            "2014-04" ["2014-04-02-first-post.md"]}))))
