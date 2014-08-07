(ns blog.article.helpers
  (:require [clojure.string :as string]))


;;; constants

(def MONTH_NAMES (.getMonths (java.text.DateFormatSymbols.)))


;;; helpers

(defn month-name [str-index]
  (get MONTH_NAMES (dec (Integer/parseInt str-index))))

(defn date->year+month+day [{:keys [date day month year] :as a}]
  (if (and (seq date)
           (or (not (seq year))
               (not (seq month))
               (not (seq day))))
    (merge a (zipmap [:year :month :day]
                     (string/split date #"-")))
    a))

(defn year+month+day->date [{:keys [date day month year] :as a}]
  (if (and (seq year)
           (seq month)
           (seq day)
           (not (seq date)))
    (assoc a :date (string/join \- [year month day]))
    a))

(defn date->string [{date :date :as a}]
  (if (instance? java.util.Date date)
    (assoc a :date (-> (java.text.SimpleDateFormat. "yyyy-MM-dd")
                       (.format date)))
    a))

(defn date+title->code [{:keys [date title code] :as a}]
  (if (and (seq date)
           (seq title)
           (not (seq code)))
    (assoc a :code (str date "-" (-> title
                                     (string/lower-case)
                                     (string/replace \space \-))))
    a))

(defn code->date+title [{:keys [date title code] :as a}]
  (if (and (seq code)
           (or (not (seq title))
               (not (seq date))))
    (let [parts (string/split code #"-")
          [date-parts title-parts] (split-at 3 parts)]
      (merge a {:date (string/join \- date-parts)
                :title (string/join \space (map string/capitalize title-parts))}))
    a))

(defn month->month-name [{m :month mn :month-name :as a}]
  (if (and (seq m)
           (not (seq mn)))
    (assoc a :month-name (month-name m))
    a))

(defn complete-article [a]
  (-> a
      date->string
      code->date+title
      date->year+month+day
      year+month+day->date
      date+title->code
      month->month-name))


;;; pagination

(defn paginate [page-num items-per-page coll]
  (let [pages (partition-all items-per-page coll)
        page-items (nth pages page-num nil)
        has-next (not (nil? (nth pages (inc page-num) nil)))
        has-previous (pos? page-num)]
    (merge {:current-page page-num
            :items page-items}
           (if has-previous {:previous-page (dec page-num)})
           (if has-next {:next-page (inc page-num)}))))

(defn pagination-urls [page-url-f page]
  (let [selected-map (select-keys page #{:previous-page :next-page})
        mapped-urls (map (fn [[k v]] [k (page-url-f v)]) selected-map)]
    (into {} mapped-urls)))
