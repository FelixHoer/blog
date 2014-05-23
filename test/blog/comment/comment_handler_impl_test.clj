(ns blog.comment.comment-handler-impl-test
  (:require [clojure.test :refer :all]
            [blog.comment.comment-handler-impl :refer :all]
            [blog.comment.comment-sql-datastore :as cdb]
            [blog.comment.comment-sql-datastore-impl :as cdbi]
            [clojure.java.jdbc :as jdbc]))


; test helper functions

(deftest helpers
  (testing "comment-name"
    (are [req n] (= (comment-name req) n)
         {}                                 ""
         {:session {:username "name"}}      "name"
         {:form-params {"comment-name" "name"}}  "name"
         {:session {:username "name1"}
          :form-params {"comment-name" "name2"}} "name1"))

  (testing "comment-text"
    (are [req t] (= (comment-text req) t)
         {} ""
         {:form-params {}} ""
         {:form-params {"comment-text" "text"}} "text")))


; test endpoints

(defn traverse [m ks]
  (reduce (fn [accu k] (k accu)) m ks))

(def db-spec {:subprotocol "hsqldb"
                :subname (str "mem:testdb"
                              ";shutdown=true"
                              ";sql.syntax_pgs=true")
                :user "SA"
                :password ""})

(deftest endpoints
  (jdbc/with-db-connection [con db-spec]
    (let [com {:db (cdb/map->CommentSQLDatastore {:db con})}]

      (testing "create-comment-table"
        (is (= (cdbi/create-comment-table {:db con})
               :ok)))

      (testing "save-comment"
        (are [code req resp] (= (save-comment com code req) resp)

             "the-article"
             {:form-params {"comment-name" ""
                            "comment-text" "text"}
              :scheme "http"
              :server-name "localhost"
              :server-port "8080"}
             {:status 302,
              :headers {"Location" "http://localhost:8080/articles/the-article"},
              :data {:flash {:warning "Could not save comment, because: Name is blank."}},
              :body ""}

             "the-article"
             {:form-params {"comment-name" "name"
                            "comment-text" "text"}
              :scheme "http"
              :server-name "localhost"
              :server-port "8080"}
             {:status 302,
              :headers {"Location" "http://localhost:8080/articles/the-article#comments"},
              :body ""}))

      (testing "extend-with-comment-counts"
        (are [resp1 resp2] (= (extend-with-comment-counts com resp1) resp2)

             {:data {:items []}}
             {:data {:items []}}

             {:data {:items [{:code "the-article"}]}}
             {:data {:items [{:code "the-article"
                              :comment-count 1}]}}

             {:data {:items [{:code "the-wrong-article"}]}}
             {:data {:items [{:code "the-wrong-article"
                              :comment-count 0}]}}))

      (testing "extend-with-comments"
        (are [resp1 checks] (let [resp2 (extend-with-comments com resp1)]
                             (every? identity
                                     (map (fn [[ks v]] (= v (traverse resp2 ks))) checks)))

             {:data {:items [{:code "the-article"}]}}
             [[[:data :items first :code] "the-article"]
              [[:data :items first :comments first :name] "name"]
              [[:data :items first :comments first :text] "text"]]

             {:data {:items [{:code "the-wrong-article"}]}}
             [[[:data :items first :code] "the-wrong-article"]
              [[:data :items first :comments] []]])))))
