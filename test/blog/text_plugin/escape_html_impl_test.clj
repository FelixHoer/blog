(ns blog.text-plugin.escape-html-impl-test
  (:use blog.text-plugin.plugin
        blog.text-plugin.escape-html)
  (:require [clojure.test :refer :all]
            [blog.text-plugin.escape-html-impl :refer :all]))


(deftest smiley
  (testing "process"
    (let [plugin (map->EscapeHTMLPlugin {})]
      (are [in out] (is (= out (process-impl plugin in)))
        "<a href=\"image.png\">title</a>"
        "&lt;a href=&quot;image.png&quot;&gt;title&lt;/a&gt;"

        "<a href=\"image.png?a=1&b=3\">title</a>"
        "&lt;a href=&quot;image.png?a=1&amp;b=3&quot;&gt;title&lt;/a&gt;"

        "![title](image.png?a=1&b=3)"
        "![title](image.png?a=1&amp;b=3)")))

  (testing "process (preserving ampersand)"
    (let [plugin (map->EscapeHTMLPlugin {:preserve-ampersand? true})]
      (are [in out] (is (= out (process-impl plugin in)))
        "<a href=\"image.png\">title</a>"
        "&lt;a href=&quot;image.png&quot;&gt;title&lt;/a&gt;"

        "<a href=\"image.png?a=1&b=3\">title</a>"
        "&lt;a href=&quot;image.png?a=1&b=3&quot;&gt;title&lt;/a&gt;"

        "![title](image.png?a=1&b=3)"
        "![title](image.png?a=1&b=3)"))))
