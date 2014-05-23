(ns blog.auth.auth-handler-impl-test
  (:use blog.auth.auth-datastore)
  (:require [clojure.test :refer :all]
            [blog.auth.auth-handler-impl :refer :all]))

; test helpers

(deftest auth-helpers
  (testing "local-redirect"
    (is (= (local-redirect {:scheme "http"
                            :server-name "localhost"
                            :server-port "8080"}
                           "/next")
           {:status 302,
            :headers {"Location" "http://localhost:8080/next"},
            :body ""}))
    (is (= (local-redirect {:scheme "http"
                            :server-name "localhost"
                            :server-port 8080}
                           "/next")
           {:status 302,
            :headers {"Location" "http://localhost:8080/next"},
            :body ""}))
    (is (= (local-redirect {:scheme "http"
                            :server-name "localhost"
                            :server-port ""}
                           "/next")
           {:status 302,
            :headers {"Location" "http://localhost/next"},
            :body ""}))
    (is (= (local-redirect {:scheme "http"
                            :server-name "localhost"}
                           "/next")
           {:status 302,
            :headers {"Location" "http://localhost/next"},
            :body ""})))

  (testing "is-logged-in?"
    (is (= (is-logged-in? {:logged-in true})
           true))))

; test endpoints

(defrecord MockAuthDB []
  AuthDatastore
    (authenticate [this username pwd]
      (and (= "username" username)
           (= "password" pwd))))

(def auth-db (map->MockAuthDB {}))

(deftest auth-endpoints
  (testing "login"
    (is (= (login {})
           {:template :login})))

  (testing "enforce-auth"
    (is (= (enforce-auth {:scheme "http"
                          :server-name "localhost"})
           {:status 302,
            :headers {"Location" "http://localhost/login"},
            :body ""})))

  (testing "process-login"
    (is (= (process-login {:scheme "http"
                           :server-name "localhost"
                           :component {:db auth-db}
                           :session {:some-key 123}
                           :form-params {"username" "username"
                                         "password" "password"}})
           {:status 302,
            :headers {"Location" "http://localhost/"},
            :session {:logged-in true
                      :username "username"}
            :data {:flash {:info "You logged in successfully!"}}
            :body ""}))
    (is (= (process-login {:scheme "http"
                           :server-name "localhost"
                           :component {:db auth-db}
                           :session {:some-key 123}
                           :form-params {"username" "username"
                                         "password" "wrong password"}})
           {:template :login,
            :data {:flash {:warning "Username and/or Password was incorrect!"}}}))))
