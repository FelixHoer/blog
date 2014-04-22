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
            :body ""})))

  (testing "is-logged-in?"
    (is (= (is-logged-in? {:logged-in true})
           true))))

(deftest auth-endpoints
  (testing "login"
    (is (= (login {})
           {:template :login})))

  (testing "enforce-auth"
    (is (= (enforce-auth {:server-name "localhost"})
           {:status 302,
            :headers {"Location" "http://localhost/login"},
            :body ""})))

  (testing "process-login"
    (is (= (process-login {:server-name "localhost"
                           :session {:some-key 123}
                           :params {:some-param 456}})
           {:status 302,
            :headers {"Location" "http://localhost/"},
            :session {:logged-in true}
            :body ""}))))
