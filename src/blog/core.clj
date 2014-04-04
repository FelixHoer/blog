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

(def MONTH_NAMES (.getMonths (java.text.DateFormatSymbols.)))


; helpers

(defn local-redirect [{server :server-name port :server-port} path]
  (ring-response/redirect (str "http://" server
                               (if (not (empty? port)) (str ":" port))
                               path)))

(defn date-code->text [code]
  (let [[year m] (string/split code #"-")
        month (get MONTH_NAMES (- (Integer/parseInt m) 1))]
    (str month " " year)))

(defn article-code->title [code]
  (let [title-code (string/join (drop 11 (drop-last 3 code)))
        words (string/split title-code #"-")
        capitalized-words (map string/capitalize words)]
    (string/join " " capitalized-words)))


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
    (map (fn [f] {:name (.getName f)
                  :path (.getPath f)})
         files)))

(defn group-by-month [files]
  (let [date-prefix-f #(string/join (take DATE_PREFIX_LENGTH (:name %)))
        grouped-files (group-by date-prefix-f files)]
    grouped-files))

(defn article-data [name]
  (let [safe-name (string/join (remove #(= \/ %) name))
        path (str ARTICLES_PATH "/" safe-name)
        file-content (slurp path)
        html (markdown/md-to-html-string file-content)]
    html))

(defn article-page-data [page-num article-files]
  (let [sorted-files (sort-by :name article-files)
        pages (partition-all ARTICLES_PER_PAGE sorted-files)
        page (nth pages page-num)
        page-names (map :name page)]
    (map article-data page-names)))

(defn article-month-data [month article-files]
  (let [month-files (get (group-by-month article-files) month)
        file-names (map :name month-files)]
    (map article-data file-names)))

(defn recent-article-data [article-files]
  (let [sorted-files (sort-by :name article-files)
        recent-titles (map :name (take RECENT_ARTICLES sorted-files))]
    (map (fn [f] {:code (string/join (drop-last 3 f) )
                  :title (article-code->title f)})
         recent-titles)))

(defn archive-article-data [article-files]
  (let [months (keys (group-by-month article-files))]
    (map (fn [m] {:code m
                  :text (date-code->text m)})
         months)))


; server endpoints

(defn login [req]
  (println "login" req)
  {:body (login-template {})})

(defn process-login [{session :session params :params :as req}]
  (println "process" req)
  ; TODO check the received credentials
  (let [resp (local-redirect req "/")]
    (assoc-in resp [:session :logged-in] true)))

(defn list-articles [session]
  (println "list")
  (let [files (article-files)
        data {:body (string/join (map #(article-partial {:body %})
                                      (article-page-data 0 files)))
              :recent-articles (recent-article-data files)
              :archive-months (archive-article-data files)}]
    {:body (list-template data)}))

(defn show-article [id session]
  (println "show" id)
  (let [files (article-files)
        data {:body (article-partial {:body (article-data (str id ".md"))})
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
  (GET  "/login" {:as req}
    (login req))
  (POST "/login" {:as req}
    (process-login req))
  (GET  "/" {session :session :as req}
    (authenticated req (list-articles session)))
  (GET  "/article/:id" {{id :id} :params session :session :as req}
    (authenticated req (show-article id session)))
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
