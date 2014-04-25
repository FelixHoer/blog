(ns blog.auth.auth-file-datastore-impl-test
  (:require [clojure.test :refer :all]
            [blog.auth.auth-file-datastore-impl :refer :all]))

(def db (-> {}
            (add-credentials "user" "foobar")
            (add-credentials "user2" "foobar2")))

(deftest file-datastore
  (testing "check-credentials"
    (is (= (check-credentials db "user" "foobar")
           true))
    (is (= (check-credentials db "user" "wrong pwd")
           false))
    (is (= (check-credentials db "wrong user" "foobar")
           false))
    (is (= (check-credentials db "user" "foobar2")
           false)))

  (testing "add-credentials"
    (let [data (add-credentials {} "username" "password")]
      (is (= (check-credentials data "username" "password")
             true))
      (is (= (check-credentials data "username" "wrong password")
             false))
      (is (= (check-credentials data "wrong username" "password")
             false)))))
