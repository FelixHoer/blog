(ns blog.article.helpers
  (:use blog.constants)
  (:require [clojure.string :as string]))


; constants

(def MONTH_NAMES (.getMonths (java.text.DateFormatSymbols.)))


; helpers

(defn code->filename [code]
  (str code EXTENSION))

(defn filename->code [filename]
  (string/join (drop-last EXTENSION_LENGTH filename)))

(defn month-name [str-index]
  (get MONTH_NAMES (dec (Integer/parseInt str-index))))

(defn parse-article-code
  "Extracts the parts of the given code into a map.
   Format for the code parameter: yyyy-mm-dd[-title-with-dashes]
   Returns a map with keys :year, :month, :day, :month-name (name of the month),
   :title (every word is capitalized) and given :code"
  [code]
  (let [parts (string/split code #"-")
        [date-parts title-parts] (split-at 3 parts)]
    (merge (zipmap [:year :month :day] date-parts)
           {:month-name (month-name (second date-parts))
            :title (string/join " " (map string/capitalize title-parts))
            :code code})))

(def parse-article-filename (comp parse-article-code filename->code))


; pagination

(defn paginate [page-num items-per-page coll]
  (let [pages (partition-all items-per-page coll)
        page-items (nth pages page-num nil)
        has-next (not (nil? (nth pages (inc page-num) nil)))
        has-previous (> page-num 0)]
    (merge {:current-page page-num
            :items page-items}
           (if has-previous {:previous-page (dec page-num)})
           (if has-next {:next-page (inc page-num)}))))

(defn pagination-urls [page-url-f page]
  (let [selected-map (select-keys page #{:previous-page :next-page})
        mapped-urls (map (fn [[k v]] [k (page-url-f v)]) selected-map)]
    (into {} mapped-urls)))
