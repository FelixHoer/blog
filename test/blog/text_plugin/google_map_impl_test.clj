(ns blog.text-plugin.google-map-impl-test
  (:require [clojure.test :refer :all]
            [blog.text-plugin.plugin :as p]
            [blog.text-plugin.google-map :as spec]
            [blog.text-plugin.google-map-impl :as impl]))

(def google-plugin (spec/map->GoogleMapPlugin {:app-key "123"}))

(deftest google-map
  (testing "process"
    (is (= (p/process google-plugin "![my map](map:Klagenfurt+Austria)")
           (impl/map-iframe "123" "my map" "Klagenfurt+Austria")
           (str "<iframe class=\"google-map\" width=\"600\" height=\"400\" frameborder=\"0\" "
                "src=\"https://www.google.com/maps/embed/v1/place?key=123"
                "&q=Klagenfurt+Austria\">"
                "</iframe>")))
    (is (= (p/process google-plugin "![my map](map:Klagenfurt+Austria|Wien+Austria)")
           (impl/map-iframe "123" "my map" "Klagenfurt+Austria|Wien+Austria")
           (str "<iframe class=\"google-map\" width=\"600\" height=\"400\" frameborder=\"0\" "
                "src=\"https://www.google.com/maps/embed/v1/directions?key=123"
                "&origin=Klagenfurt+Austria"
                "&destination=Wien+Austria\">"
                "</iframe>")))
    (is (= (p/process google-plugin "![my map](map:Klagenfurt+Austria|Graz+Austria|Wien+Austria)")
           (impl/map-iframe "123" "my map" "Klagenfurt+Austria|Graz+Austria|Wien+Austria")
           (str "<iframe class=\"google-map\" width=\"600\" height=\"400\" frameborder=\"0\" "
                "src=\"https://www.google.com/maps/embed/v1/directions?key=123"
                "&origin=Klagenfurt+Austria"
                "&destination=Wien+Austria"
                "&waypoints=Graz+Austria\">"
                "</iframe>"))))

  (testing "build-iframe"
    (is (= (impl/build-iframe "QUERY")
           (str "<iframe class=\"google-map\" width=\"600\" height=\"400\" frameborder=\"0\" "
                "src=\"https://www.google.com/maps/embed/v1/QUERY\">"
                "</iframe>"))))

  (testing "build-query"
    (is (= (impl/build-query "123" ["WP1"])
           "place?key=123&q=WP1"))
    (is (= (impl/build-query "123" ["WP1" "WP2" "WP3" "WP4"])
           "directions?key=123&origin=WP1&destination=WP4&waypoints=WP2|WP3"))))
