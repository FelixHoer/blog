(ns blog.text-plugin.dropbox-impl-test
  (:require [clojure.test :refer :all]
            [blog.text-plugin.plugin :as plugin]
            [blog.text-plugin.dropbox :as spec]))

(def dropbox-plugin (spec/map->DropboxPlugin {:user-id "<USER-ID>"}))

(deftest dropbox
  (testing "process"
    (is (= (plugin/process dropbox-plugin "")
           ""))
    (is (= (plugin/process dropbox-plugin "![<TITLE>](db:<URL>)")
           "![<TITLE>](http://dl.dropboxusercontent.com/u/<USER-ID>/<URL>)"))
    (is (= (plugin/process dropbox-plugin "![<TITLE1>](db:<URL1>) ![<TITLE2>](db:<URL2>)")
           (str "![<TITLE1>](http://dl.dropboxusercontent.com/u/<USER-ID>/<URL1>)" " "
                "![<TITLE2>](http://dl.dropboxusercontent.com/u/<USER-ID>/<URL2>)")))))
