(ns blog.article-plugin.dropbox-impl-test
  (:use blog.article-plugin.plugin
        blog.article-plugin.dropbox)
  (:require [clojure.test :refer :all]
            [blog.article-plugin.dropbox-impl :refer :all]))

(def dropbox-plugin (map->DropboxPlugin {:user-id "<USER-ID>"}))

(deftest dropbox
  (testing "process"
    (is (= (process dropbox-plugin "")
           ""))
    (is (= (process dropbox-plugin "![<TITLE>](db:<URL>)")
           "![<TITLE>](http://dl.dropboxusercontent.com/u/<USER-ID>/<URL>)"))
    (is (= (process dropbox-plugin "![<TITLE1>](db:<URL1>) ![<TITLE2>](db:<URL2>)")
           (str "![<TITLE1>](http://dl.dropboxusercontent.com/u/<USER-ID>/<URL1>)" " "
                "![<TITLE2>](http://dl.dropboxusercontent.com/u/<USER-ID>/<URL2>)")))))
