(ns blog.article.article-file-datastore-impl-test
  (:require [clojure.test :refer :all]
            [blog.article.article-file-datastore-impl :refer :all]))

(def db-component {:recent-articles 5})

(def test-article-names
  ["2014-05-01-next-post.md"
   "2014-04-15-april-post.md"
   "2014-04-02-first-post.md"])

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
            :day "02", :month "04", :year "2014",
            :date "2014-04-02"})))

  (testing "group-by-month"
    (is (= (group-by-month test-article-names)
           {"2014-05" ["2014-05-01-next-post.md"],
            "2014-04" ["2014-04-15-april-post.md"
                       "2014-04-02-first-post.md"]})))

  (testing "recent-article-data"
    (is (= (recent-article-data db-component test-article-names)
           [{:code "2014-05-01-next-post", :title "Next Post",
             :month-name "May", :day "01", :month "05", :year "2014",
             :date "2014-05-01"}
            {:code "2014-04-15-april-post", :title "April Post",
             :month-name "April", :day "15", :month "04", :year "2014",
             :date "2014-04-15"}
            {:code "2014-04-02-first-post", :title "First Post",
             :month-name "April", :day "02", :month "04", :year "2014",
             :date "2014-04-02"}])))

  (testing "archive-data"
    (is (= (archive-data db-component test-article-names)
           [{:code "2014-05",
             :month-name "May", :month "05", :year "2014"
             :title "", :date "2014-05"}
            {:code "2014-04",
             :month-name "April", :month "04", :year "2014"
             :title "", :date "2014-04"}]))))
