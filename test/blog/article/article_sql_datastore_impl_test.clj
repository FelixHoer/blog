(ns blog.article.article-sql-datastore-impl-test
  (:require [clojure.test :refer :all]
            [blog.article.article-sql-datastore-impl :refer :all]
            [clojure.java.jdbc :as jdbc]))


(deftest sql-datastore
  (let [db-spec {:subprotocol "hsqldb"
                 :subname (str "mem:testdb"
                               ";shutdown=true"
                               ";sql.syntax_pgs=true")
                 :user "SA"
                 :password ""}]
    (jdbc/with-db-connection [con db-spec]
      (let [com {:db con
                 :articles-per-page 5
                 :recent-articles 10}]

        (testing "create-article-table"
          (is (= (create-article-table com)
                 :ok)))

        (testing "add-article"
          (is (= (add-article com {:name "2014-06-02-title"
                                   :date (.getTime (java.util.GregorianCalendar. 2014 (dec 6) 2))
                                   :body "body"})
                 :ok)))

        (testing "article-page"
          (is (= (article-page com 0)
                 {:current-page 0,
                  :items [{:body "body",
                           :code "2014-06-02-title",
                           :title "Title",
                           :month-name "June",
                           :day "02",
                           :month "06",
                           :year "2014"}]})))

        (testing "article-month-page"
          (is (= (article-month-page com "2014-06" 0)
                 {:current-page 0,
                  :items [{:body "body",
                           :code "2014-06-02-title",
                           :title "Title",
                           :month-name "June",
                           :day "02",
                           :month "06",
                           :year "2014"}]})))

        (testing "article"
          (is (= (article com "2014-06-02-title")
                 {:current-page 0
                  :items [{:body "body",
                           :code "2014-06-02-title",
                           :title "Title",
                           :month-name "June",
                           :day "02",
                           :month "06",
                           :year "2014"}]})))

        (testing "article-impl"
          (is (= (article-impl com "2014-06-02-title")
                 {:archive-months [{:code "2014-06",
                                    :title "",
                                    :month-name "June",
                                    :month "06",
                                    :year "2014"}],
                  :recent-articles [{:code "2014-06-02-title",
                                     :title "Title",
                                     :month-name "June",
                                     :day "02",
                                     :month "06",
                                     :year "2014"}],
                  :items [{:body "body",
                           :code "2014-06-02-title",
                           :title "Title",
                           :month-name "June",
                           :day "02",
                           :month "06",
                           :year "2014"}],
                  :current-page 0})))

          (testing "article-page-impl"
            (is (= (article-page-impl com 0)
                   {:archive-months [{:code "2014-06",
                                      :title "",
                                      :month-name "June",
                                      :month "06",
                                      :year "2014"}],
                    :recent-articles [{:code "2014-06-02-title",
                                       :title "Title",
                                       :month-name "June",
                                       :day "02",
                                       :month "06",
                                       :year "2014"}],
                    :current-page 0,
                    :items [{:body "body",
                             :code "2014-06-02-title",
                             :title "Title",
                             :month-name "June",
                             :day "02",
                             :month "06",
                             :year "2014"}]})))

          (testing "article-month-page-impl"
            (is (= (article-month-page-impl com "2014-06" 0)
                   {:archive-months [{:code "2014-06",
                                      :title "",
                                      :month-name "June",
                                      :month "06",
                                      :year "2014"}],
                    :recent-articles [{:code "2014-06-02-title",
                                       :title "Title",
                                       :month-name "June",
                                       :day "02",
                                       :month "06",
                                       :year "2014"}],
                    :current-page 0,
                    :items [{:body "body",
                             :code "2014-06-02-title",
                             :title "Title",
                             :month-name "June",
                             :day "02",
                             :month "06",
                             :year "2014"}]})))))))
