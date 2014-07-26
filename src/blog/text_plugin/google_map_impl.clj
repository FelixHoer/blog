(ns blog.text-plugin.google-map-impl
  (:require [clojure.string :as string]))

(defmulti build-query (fn [_ wps] (if (= 1 (count wps))
                                    :single
                                    :multi)))

(defmethod build-query :single [app-key waypoints]
  (str "place"
       "?key=" app-key
       "&q=" (first waypoints)))

(defmethod build-query :multi [app-key waypoints]
  (str "directions"
       "?key=" app-key
       "&origin=" (first waypoints)
       "&destination=" (last waypoints)
       (if-let [wps (not-empty (drop 1 (drop-last waypoints)))]
         (str "&waypoints=" (string/join "|" wps)))))

(defn build-iframe [query]
  (str "<iframe"
       " class=\"google-map\""
       " width=\"600\""
       " height=\"400\""
       " frameborder=\"0\""
       " src=\"https://www.google.com/maps/embed/v1/" query "\""
       ">"
       "</iframe>"))

(defn map-iframe [app-key title data-str]
  (let [waypoints (string/split data-str #"\|")
        query (build-query app-key waypoints)
        iframe (build-iframe query)]
    iframe))

(defn process [{app-key :app-key} content]
  (string/replace content
                  #"\!\[([^\)]*)\]\(map:([^\)]+)\)"
                  (fn [[_ title data]] (map-iframe app-key title data))))
