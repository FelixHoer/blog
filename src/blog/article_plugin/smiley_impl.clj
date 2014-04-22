(ns blog.article-plugin.smiley-impl
  (:require [clojure.string :as string]))

(def SMILEYS [[#"(0|O):-?\)|:angel:"  "face-angel.png"]
              [#":'\(|:cry:"          "face-crying.png"]
              [#">:\)|:devil:"        "face-devilish.png"]
              [#"B-?\)|:glasses:"     "face-glasses.png"]
              [#":-?D|:grin:"         "face-grin.png"]
              [#":-?\*|:kiss:"        "face-kiss.png"]
              [#":-?(\|\/):plain:"    "face-plain.png"]
              [#":-?\(|:sad:"         "face-sad.png"]
              [#":-?\)|:smile:"       "face-smile.png"]
              [#":-?(o|O)|:surprise:" "face-surprise.png"]
              [#";-?\)|:wink:"        "face-wink.png"]
              [#"<3|:love:"           "emblem-favorite.png"]])

(defn smiley-tag [image]
  (str "<img src=\"/smiley/" image "\" />"))

(defn apply-smiley [content [regex image]]
  (string/replace content regex (smiley-tag image)))

(defn process-impl [this content]
  (reduce apply-smiley content SMILEYS))
