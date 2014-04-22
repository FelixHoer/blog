(ns blog.auth.auth-handler-impl-test
  (:require [clojure.test :refer :all]
            [blog.auth.auth-handler-impl :refer :all]))

(deftest auth-helpers
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
            :body ""}))))
