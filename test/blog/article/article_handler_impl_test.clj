(ns blog.article.article-handler-impl-test
  (:use blog.text-plugin.plugin
        blog.article.article-datastore)
  (:require [clojure.test :refer :all]
            [blog.article.article-handler-impl :refer :all]))

; test plugins

(defrecord AddIdPlugin [id]
  Plugin
    (process [this content] (str content id)))

(def plugin-1 (map->AddIdPlugin {:id 1}))
(def plugin-2 (map->AddIdPlugin {:id 2}))
(def plugin-3 (map->AddIdPlugin {:id 3}))

(def plugins-component {:first-plugin  plugin-1
                        :second-plugin plugin-2
                        :third-plugin  plugin-3
                        :plugins [:first-plugin :second-plugin :third-plugin]})

(deftest plugins
  (testing "plugin-seq"
    (is (= (plugin-seq plugins-component)
           [plugin-1 plugin-2 plugin-3])))

  (testing "apply-plugins"
    (is (= (apply-plugins "abc" [plugin-1 plugin-2 plugin-3])
           "abc123")))

  (testing "apply-plugins-page"
    (is (= (apply-plugins-page plugins-component
                               {:items [{:body "abc"}
                                        {:body "def"}
                                        {:body "ghi"}]})
           {:items [{:body "abc123"}
                    {:body "def123"}
                    {:body "ghi123"}]}))))


(defrecord MockArticleDB []
  ArticleDatastore
    (article [this code]
      (if (= "some-code" code)
        {:items [{:body "abc"}]
         :recent-articles "recent-articles"
         :archive-months "archive-months"}))
    (article-page [this page-num]
      (if (= 0 page-num)
        {:current-page page-num
         :next-page 1
         :items [{:body "abc"} {:body "def"}]
         :recent-articles "recent-articles"
         :archive-months "archive-months"}))
    (article-month-page [this month page-num]
      (if (= 0 page-num)
        {:current-page page-num
         :next-page 1
         :items [{:body "def"} {:body "ghi"}]
         :recent-articles "recent-articles"
         :archive-months "archive-months"})))

; test endpoints

(def endpoints-component (merge plugins-component
                                {:db (map->MockArticleDB {})}))

(deftest endpoints
  (testing "list-articles-page"
    (is (= (list-articles-page endpoints-component 0)
           {:data {:current-page 0,
                   :next-page "/articles/page/1",
                   :items '({:body "abc123"} {:body "def123"})
                   :recent-articles "recent-articles"
                   :archive-months "archive-months"},
            :template :article-list})))

  (testing "list-articles-month-page"
    (is (= (list-articles-month-page endpoints-component "2014-04" 0)
           {:data {:current-page 0,
                   :next-page "/articles/month/2014-04/page/1",
                   :items '({:body "def123"} {:body "ghi123"})
                   :recent-articles "recent-articles"
                   :archive-months "archive-months"},
            :template :article-list})))

  (testing "show-article"
    (is (= (show-article endpoints-component "some-code")
           {:data {:items '({:body "abc123"}),
                   :recent-articles "recent-articles",
                   :archive-months "archive-months"},
            :template :article}))))
