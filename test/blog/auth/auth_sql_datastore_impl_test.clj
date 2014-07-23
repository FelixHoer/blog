(ns blog.auth.auth-sql-datastore-impl-test
  (:require [clojure.test :refer :all]
            [blog.auth.auth-sql-datastore-impl :refer :all]
            [clojure.java.jdbc :as jdbc]))


(deftest sql-datastore
  (let [db-spec {:subprotocol "hsqldb"
                 :subname (str "mem:testdb"
                               ";shutdown=true"
                               ";sql.syntax_pgs=true")
                 :user "SA"
                 :password ""}]
    (jdbc/with-db-connection [con db-spec]

      (testing "create-auth-table"
        (is (= (create-auth-table {:db con})
               :ok)))

      (testing "add-credentials"
        (is (= (add-credentials {:db con}
                                "correct user"
                                "correct password"
                                true)
               :ok)))

      (testing "authenticate-impl"
        (is (authenticate {:db con}
                          "correct user"
                          "correct password"))
        (is (not (authenticate {:db con}
                               "wrong user"
                               "correct password")))
        (is (not (authenticate {:db con}
                               "correct user"
                               "wrong password")))))))


(deftest management-datastore
  (let [db-spec {:subprotocol "hsqldb"
                 :subname (str "mem:testdb"
                               ";shutdown=true"
                               ";sql.syntax_pgs=true")
                 :user "SA"
                 :password ""}]
    (jdbc/with-db-connection [con db-spec]

      (testing "create-auth-table"
        (is (= (create-auth-table {:db con})
               :ok)))

      ; signup
      (testing "sign-up"
        (is (= (sign-up {:db con}
                        "correct user"
                        "correct password")
               :ok)))
      (testing "sign-up again with same name"
        (is (= (sign-up {:db con}
                        "correct user"
                        "correct password")
               :already-taken)))

      ; list
      (testing "list-users"
        (is (= (list-users {:db con})
               [{:user "correct user"
                 :confirmed false}])))

      ; confirm
      (testing "confirm user"
        (is (= (confirm-user {:db con} "correct user")
               :ok)))

      (testing "confirm user again"
        (is (= (confirm-user {:db con} "correct user")
               :ok)))

      (testing "confirm wrong user"
        (is (= (confirm-user {:db con} "wrong user")
               :fail)))

      ; list
      (testing "list-users"
        (is (= (list-users {:db con})
               [{:user "correct user"
                 :confirmed true}])))

      ; delete
      (testing "delete user"
        (is (= (delete-user {:db con} "correct user")
               :ok)))

      (testing "delete wrong user"
        (is (= (delete-user {:db con} "wrong user")
               :fail)))

      ; list
      (testing "list-users"
        (is (= (list-users {:db con})
               []))))))
