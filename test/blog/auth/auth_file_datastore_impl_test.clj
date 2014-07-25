(ns blog.auth.auth-file-datastore-impl-test
  (:require [clojure.test :refer :all]
            [blog.auth.auth-file-datastore-impl :refer :all]
            [blog.auth.auth-datastore :as spec]
            [crypto.password.scrypt :as password]))

(defmacro with-file [n & forms]
  `(let [~n (str "/tmp/test-file-" (rand-int 100000) ".test")]
    (try
      ~@forms
      (finally
       (try (clojure.java.io/delete-file ~n)
         (catch Exception e#))))))

(deftest key-derivation-function
  (testing "get-checked-user"
    (let [user  {:role :user
                 :key (password/encrypt "foobar")}
          user2 {:role :user
                 :key (password/encrypt "foobar2")}
          data {"user"  user
                "user2" user2}]
      (is (= (get-checked-user data "user" "foobar")
             (spec/map->User {:username "user"
                              :confirmed true
                              :role :user})))
      (is (= (get-checked-user data "user" "wrong pwd")
             nil))
      (is (= (get-checked-user data "wrong user" "foobar")
             nil))
      (is (= (get-checked-user data "user" "foobar2")
             nil))))

  (testing "add-credentials"
    (let [data (add-user {} "username" "password" :user)]
      (is (= (get-checked-user data "username" "password")
             (spec/map->User {:username "username"
                              :confirmed true
                              :role :user})))
      (is (= (get-checked-user data "username" "wrong password")
             nil))
      (is (= (get-checked-user data "wrong username" "password")
             nil)))))

(deftest file-datastore
  (testing "read-auth-file"
    (is (= (read-auth-file "/tmp/some-nonexistent-file.test")
           nil)))

  (testing "read-auth-file and write-auth-file"
    (let [data {"user" {:role :user
                        :key "some$password$string"}}]
      (is (with-file fpath
            (write-auth-file data fpath)
            (= (read-auth-file fpath)
               data)))))

  (testing "add-user-to-file and authenticate"
    (with-file fpath
      (is (not (authenticate {:path "/tmp/wrong-file.test"} "user" "pwd")))
      (is (not (authenticate {:path fpath} "user" "pwd")))
      (add-user-to-file {:path fpath} "user" "pwd" :user) ; sideeffect!
      (is (= (authenticate {:path fpath} "user" "pwd")
             (spec/map->User {:username "user"
                              :confirmed true
                              :role :user})))
      (is (not (authenticate {:path fpath} "user" "wrong pwd"))))))
