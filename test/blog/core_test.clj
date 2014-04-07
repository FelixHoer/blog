(ns blog.core-test
  (:require [clojure.test :refer :all]
            [blog.core :refer :all]))

(def test-articles
  ["2014-04-02-first-post.md"
   "2014-05-01-next-post.md"])

(deftest data
  (testing "group-by-month"
    (is (= (group-by-month test-articles)
           {"2014-05" ["2014-05-01-next-post.md"],
            "2014-04" ["2014-04-02-first-post.md"]}))))

(deftest helpers
  (testing "local-redirect"
    (is (= (local-redirect {:server-name "localhost"
                            :server-port "8080"}
                           "/next")
           {:status 302,
            :headers {"Location" "http://localhost:8080/next"},
            :body ""}))
    (is (= (local-redirect {:server-name "localhost"
                            :server-port 8080}
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
  (testing "parse-article-code"
    (is (let [res (parse-article-code "2014-02")]
           (and (= (:month-name res) "February")
                (= (:year res) "2014"))))
    (is (= (parse-article-code "2014-04-02-first-post")
           {:code "2014-04-02-first-post",
            :title "First Post",
            :month-name "April",
            :day "02", :month "04", :year "2014"}))))
