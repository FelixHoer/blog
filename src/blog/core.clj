(ns blog.core
  (:use compojure.core)
  (:require [clojure.string :as string]
            [compojure.route :as route]
            [ring.middleware.session :as session]
            [ring.middleware.params :as params]
            [ring.util.response :as ring-response]
            [clostache.parser :as clostache]))

; constants

(def STATIC_RESOURCE_PATH "static")
(def TEMPLATES_RESOURCE_PATH "templates")

; helpers

(defn local-redirect [{server :server-name port :server-port} path]
  (ring-response/redirect (str "http://" server (if port (str ":" port)) path)))

(str nil)

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
  {:body (list-template {:body "some articles..."})})

(defn show-article [id session]
  {:body (article-template {:id id})})

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
