(ns blog.comment.comment-sql-datastore-impl-test
  (:require [clojure.test :refer :all]
            [blog.comment.comment-sql-datastore-impl :refer :all]
            [clojure.java.jdbc :as jdbc]))


; test the validation function

(deftest validation
  (testing "validation-errors"
    (are [c errs] (= (validation-errors c) errs)

         nil            ["Name is blank."
                         "Text is blank."]
         {}             ["Name is blank."
                         "Text is blank."]

         {:name "name"} ["Text is blank."]
         {:name "name"
          :text ""}     ["Text is blank."]
         {:name "name"
          :text "    "} ["Text is blank."]

         {:text "text"} ["Name is blank."]
         {:name ""
          :text "text"} ["Name is blank."]
         {:name "    "
          :text "text"} ["Name is blank."]

         {:name (apply str (repeat 256 \a))
          :text "text"} ["Name is too long."]

         {:name "name"
          :text "text"} nil)))


; test functions that operate on the database

(deftest database-operations
  (let [db-spec {:subprotocol "hsqldb"
                :subname (str "mem:testdb"
                              ";shutdown=true"
                              ";sql.syntax_pgs=true")
                :user "SA"
                :password ""}]
    (jdbc/with-db-connection [con db-spec]

      (testing "create-comment-table"
        (is (= (create-comment-table {:db con})
               :ok)))

      (testing "insert-comment"
        (is (= (insert-comment {:db con}
                               {:name "some author"
                                :text "some text"
                                :time (java.util.Date.)}
                       "the-article")
               :ok)))

      (testing "select-comments"
        (let [[c] (select-comments {:db con} "the-article")]
          (are [k v] (= (k c) v)
               :name "some author"
               :text "some text"))
        (is (= (select-comments {:db con} "the-wrong-article")
               [])))

      (testing "select-comment-count"
        (is (= (select-comment-count con "the-article")
               1))
        (is (= (select-comment-count con "the-wrong-article")
               0)))

      (testing "select-comment-counts"
        (is (= (select-comment-counts {:db con} ["the-article"])
               {"the-article" 1}))
        (is (= (select-comment-counts {:db con} ["the-wrong-article"])
               {"the-wrong-article" 0}))))))
