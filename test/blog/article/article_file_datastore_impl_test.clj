(ns blog.article.article-file-datastore-impl-test
  (:require [clojure.test :refer :all]
            [blog.article.article-file-datastore-impl :refer :all]))

(def test-articles
  ["2014-04-02-first-post.md"
   "2014-05-01-next-post.md"])

(deftest helpers
  (testing "code->filename"
    (is (= (code->filename "some-article")
           "some-article.md")))
  (testing "filename->code"
    (is (= (filename->code "some-article.md")
           "some-article")))
  (testing "parse-article-filename"
    (is (= (parse-article-filename "2014-04-02-first-post.md")
           {:code "2014-04-02-first-post",
            :title "First Post",
            :month-name "April",
            :day "02", :month "04", :year "2014"})))
  (testing "group-by-month"
    (is (= (group-by-month test-articles)
           {"2014-05" ["2014-05-01-next-post.md"],
            "2014-04" ["2014-04-02-first-post.md"]}))))
