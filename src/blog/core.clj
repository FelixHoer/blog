(ns blog.core
  (:use compojure.core)
  (:require [clojure.string :as string]
            [compojure.route :as route]
            [ring.middleware.session :as session]
            [ring.middleware.params :as params]
            [ring.util.response :as ring-response]
            [clostache.parser :as clostache]
            [markdown.core :as markdown]))


; constants

(def STATIC_RESOURCE_PATH "static")
(def TEMPLATES_RESOURCE_PATH "templates")
(def ARTICLES_PATH "articles")

(def ARTICLES_PER_PAGE 5)
(def RECENT_ARTICLES 10)
(def DATE_PREFIX_LENGTH 7)
(def EXTENSION ".md")
(def EXTENSION_LENGTH (count EXTENSION))

(def MONTH_NAMES (.getMonths (java.text.DateFormatSymbols.)))


; helpers

(defn local-redirect [{server :server-name port :server-port} path]
  (ring-response/redirect (str "http://" server
                               (if (not (empty? (str port))) (str ":" port))
                               path)))

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


; templates

(defn template-path [subpath]
  (str TEMPLATES_RESOURCE_PATH "/" subpath ".mustache"))

(defn template [subpath]
  (fn [data]
    (clostache/render-resource (template-path subpath) data)))

(defn templates [& subpaths]
  (let [ts (map template (reverse subpaths))
        iterate-data (fn [data t] (assoc data :body (t data)))]
    (letfn [(generate-template-content
              ([] (generate-template-content {}))
              ([data] (:body (reduce iterate-data data ts))))]
      generate-template-content)))

(def login-template   (templates "layout" "login"))
(def list-template    (templates "layout" "list"))
(def article-template (templates "layout" "article"))

(def article-partial (templates "article"))


; logic

(defn article-files []
  (let [root (clojure.java.io/file ARTICLES_PATH)
        files (filter #(.isFile %) (file-seq root))]
    (map #(.getName %) files)))

(defn group-by-month [files]
  (let [date-prefix-f #(string/join (take DATE_PREFIX_LENGTH %))
        grouped-files (group-by date-prefix-f files)]
    grouped-files))

(defn article-data [name]
  (let [safe-name (string/join (remove #(= \/ %) name))
        path (str ARTICLES_PATH "/" safe-name)
        file-content (slurp path)
        html (markdown/md-to-html-string file-content)]
    (merge (parse-article-filename name) {:body html})))

;(article-data "2014-04-02-first-post.md")

(defn article-page-data [page-num article-files]
  (let [sorted-files (reverse (sort article-files))
        data (paginate page-num ARTICLES_PER_PAGE sorted-files)]
    (update-in data [:items] #(map article-data %))))

;(article-page-data 0 (article-files))

(defn article-month-page-data [month page-num article-files]
  (let [month-files (get (group-by-month article-files) month)]
    (article-page-data page-num month-files)))

;(article-month-page-data "2014-04" 0 (article-files))

(defn sidebar-recent-article-data [article-files]
  (let [sorted-files (reverse (sort article-files))
        recent-files (take RECENT_ARTICLES sorted-files)]
    (map parse-article-filename recent-files)))

;(sidebar-recent-article-data (article-files))

(defn sidebar-archive-data [article-files]
  (let [months (keys (group-by-month article-files))
        sorted-months (reverse (sort months))]
    (map parse-article-code sorted-months)))

;(sidebar-archive-data (article-files))

(defn sidebar-data [article-files]
  {:recent-articles (sidebar-recent-article-data article-files)
   :archive-months (sidebar-archive-data article-files)})

;(sidebar-data (article-files))


; server endpoints

(defn login [req]
  (println "login")
  {:body (login-template {})})

(defn process-login [{session :session params :params :as req}]
  (println "process")
  ; TODO check the received credentials
  (let [resp (local-redirect req "/")]
    (assoc-in resp [:session :logged-in] true)))

(defn list-articles-page [page session]
  (println "list all" page)
  (let [files (article-files)
        page (article-page-data page files)
        data (merge (pagination-urls #(str "/articles/page/" %) page)
                    {:body (string/join (map article-partial (:items page)))}
                    (sidebar-data files))]
    {:body (list-template data)}))

(defn list-articles-month-page [month page session]
  (println "list month" month page)
  (let [files (article-files)
        page (article-month-page-data month page files)
        data (merge (pagination-urls #(str "/articles/month/" month "/page/" %) page)
                    {:body (string/join (map article-partial (:items page)))}
                    (sidebar-data files))]
    {:body (list-template data)}))

(defn show-article [code session]
  (println "show" code)
  (let [files (article-files)
        data {:body (article-partial (article-data (code->filename code)))
              :recent-articles (recent-article-data files)
              :archive-months (archive-article-data files)}]
    {:body (list-template data)}))


; authentication

(defn is-logged-in? [session]
  (:logged-in session))

(defmacro authenticated [req form]
  `(if-not (is-logged-in? (:session ~req))
    (local-redirect ~req "/login")
    ~form))


; routes

(defroutes blog-routes
  (GET  "/login"
        {:as req}
    (login req))
  (POST "/login"
        {:as req}
    (process-login req))
  (GET  "/"
        {session :session :as req}
    (authenticated req
      (list-articles-page 0 session)))
  (GET  "/articles"
        {session :session :as req}
    (authenticated req
      (list-articles-page 0 session)))
  (GET  "/articles/page/:page"
        {{page :page} :params session :session :as req}
    (authenticated req
      (list-articles-page (Integer/parseInt page) session)))
  (GET  "/articles/month/:month"
        {{month :month} :params session :session :as req}
    (authenticated req
      (list-articles-month-page month 0 session)))
  (GET  "/articles/month/:month/page/:page"
        {{month :month page :page} :params session :session :as req}
    (authenticated req
      (list-articles-month-page month (Integer/parseInt page) session)))
  (GET  "/articles/:code"
        {{code :code} :params session :session :as req}
    (authenticated req (show-article code session)))
  (route/resources "/" {:root STATIC_RESOURCE_PATH})
  (route/not-found
    "Page not found"))


; ring setup

(def blog-app
  (-> blog-routes
      (session/wrap-session)
      (params/wrap-params)))

(defn -main [port]
  (println "main"))
