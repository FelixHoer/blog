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
                                "correct password")
               :ok)))

      (testing "authenticate-impl"
        (is (authenticate-impl {:db con}
                               "correct user"
                               "correct password"))
        (is (not (authenticate-impl {:db con}
                                    "wrong user"
                                    "correct password")))
        (is (not (authenticate-impl {:db con}
                                    "correct user"
                                    "wrong password")))))))
