(ns blog.article.helpers-test
  (:require [clojure.test :refer :all]
            [blog.article.helpers :refer :all]))

(deftest helpers
  (testing "month-name"
    (is (= (month-name "02")
           "February")))
  (testing "parse-article-code"
    (is (let [res (parse-article-code "2014-02")]
           (and (= (:month-name res) "February")
                (= (:year res) "2014"))))
    (is (= (parse-article-code "2014-04-02-first-post")
           {:code "2014-04-02-first-post",
            :title "First Post",
            :month-name "April",
            :day "02", :month "04", :year "2014"}))))

(deftest pagination
  (testing "paginate"
    (is (= (paginate 0 3 [1 2 3 4 5 6 7 8])
           {:next-page 1
            :current-page 0
            :items '(1 2 3)}))
    (is (= (paginate 1 3 [1 2 3 4 5 6 7 8])
           {:next-page 2
            :previous-page 0
            :current-page 1
            :items '(4 5 6)}))
    (is (= (paginate 2 3 [1 2 3 4 5 6 7 8])
           {:previous-page 1
            :current-page 2
            :items '(7 8)})))
  (testing "pagination-urls"
    (is (= (pagination-urls #(str "/some/url/page/" %)
                            {:next-page 10 :previous-page 8 :items '(1 2 3)})
           {:next-page "/some/url/page/10"
            :previous-page "/some/url/page/8"}))))
