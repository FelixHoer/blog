(ns blog.auth.auth-file-datastore-impl-test
  (:require [clojure.test :refer :all]
            [blog.auth.auth-file-datastore-impl :refer :all]
            [crypto.password.scrypt :as password]))

(defmacro with-file [n & forms]
  `(let [~n (str "/tmp/test-file-" (rand-int 100000) ".test")]
    (try
      ~@forms
      (finally
       (try (clojure.java.io/delete-file ~n)
         (catch Exception e#))))))

(deftest key-derivation-function
  (testing "check-credentials"
    (let [data {"user"  (password/encrypt "foobar")
                "user2" (password/encrypt "foobar2")}]
      (is (= (check-credentials data "user" "foobar")
             true))
      (is (= (check-credentials data "user" "wrong pwd")
             false))
      (is (= (check-credentials data "wrong user" "foobar")
             false))
      (is (= (check-credentials data "user" "foobar2")
             false))))

  (testing "add-credentials"
    (let [data (add-credentials {} "username" "password")]
      (is (= (check-credentials data "username" "password")
             true))
      (is (= (check-credentials data "username" "wrong password")
             false))
      (is (= (check-credentials data "wrong username" "password")
             false)))))

(deftest file-datastore
  (testing "read-auth-file"
    (is (= (read-auth-file "/tmp/some-nonexistent-file.test")
           {})))

  (testing "read-auth-file and write-auth-file"
    (let [data {"user" "some$password$string"}]
      (is (with-file fpath
            (write-auth-file data fpath)
            (= (read-auth-file fpath)
               data)))))

  (testing "add-credentials-to-file and check-credentials-in-file"
    (with-file fpath
      (is (not (check-credentials-in-file "/tmp/wrong-file.test" "user" "pwd")))
      (is (not (check-credentials-in-file fpath "user" "pwd")))
      (add-credentials-to-file fpath "user" "pwd") ; sideeffect!
      (is (check-credentials-in-file fpath "user" "pwd")
      (is (not (check-credentials-in-file fpath "user" "wrong-pwd")))))))
