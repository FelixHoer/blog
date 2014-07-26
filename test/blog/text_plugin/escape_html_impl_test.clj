(ns blog.text-plugin.escape-html-impl-test
  (:require [clojure.test :refer :all]
            [blog.text-plugin.plugin :as p]
            [blog.text-plugin.escape-html :as spec]))


(deftest smiley
  (testing "process"
    (let [plugin (spec/map->EscapeHTMLPlugin {})]
      (are [in out] (is (= out (p/process plugin in)))
        "<a href=\"image.png\">title</a>"
        "&lt;a href=&quot;image.png&quot;&gt;title&lt;/a&gt;"

        "<a href=\"image.png?a=1&b=3\">title</a>"
        "&lt;a href=&quot;image.png?a=1&amp;b=3&quot;&gt;title&lt;/a&gt;"

        "![title](image.png?a=1&b=3)"
        "![title](image.png?a=1&amp;b=3)")))

  (testing "process (preserving ampersand)"
    (let [plugin (spec/map->EscapeHTMLPlugin {:preserve-ampersand? true})]
      (are [in out] (is (= out (p/process plugin in)))
        "<a href=\"image.png\">title</a>"
        "&lt;a href=&quot;image.png&quot;&gt;title&lt;/a&gt;"

        "<a href=\"image.png?a=1&b=3\">title</a>"
        "&lt;a href=&quot;image.png?a=1&b=3&quot;&gt;title&lt;/a&gt;"

        "![title](image.png?a=1&b=3)"
        "![title](image.png?a=1&b=3)"))))
