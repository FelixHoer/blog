(defproject blog "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [ring/ring-core "1.2.2"]
                 [ring/ring-jetty-adapter "1.2.2"]
                 [compojure "1.1.6"]
                 [de.ubercode.clostache/clostache "1.3.1"]
                 [markdown-clj "0.9.41"]]
  :uberjar-name "blog-standalone.jar")
