(ns blog.core-test
  (:require [clojure.test :refer :all]
            [blog.core :refer :all]))

(def test-articles
  [{:name "2014-04-02-first-post.md",
    :path "articles/2014-04-02-first-post.md"}
   {:name "2014-05-01-next-post.md",
    :path "articles/2014-05-01-next-post.md"}])

(deftest data
  (testing "group-by-month"
    (is (= (group-by-month test-articles)
           {"2014-05" [{:name "2014-05-01-next-post.md",
                        :path "articles/2014-05-01-next-post.md"}],
            "2014-04" [{:name "2014-04-02-first-post.md",
                        :path "articles/2014-04-02-first-post.md"}]}))))

(deftest helpers
  (testing "local-redirect"
    (is (= (local-redirect {:server-name "localhost"
                            :server-port "8080"}
                           "/next")
           {:status 302,
            :headers {"Location" "http://localhost:8080/next"},
            :body ""}))
    (is (= (local-redirect {:server-name "localhost"
                            :server-port ""}
                           "/next")
           {:status 302,
            :headers {"Location" "http://localhost/next"},
            :body ""}))
    (is (= (local-redirect {:server-name "localhost"}
                           "/next")
           {:status 302,
            :headers {"Location" "http://localhost/next"},
            :body ""})))
  (testing "date-code->text"
    (is (= (date-code->text "2014-02")
           "February 2014")))
  (testing "article-code->title"
    (is (= (article-code->title "2014-04-02-first-post.md")
           "First Post"))))
