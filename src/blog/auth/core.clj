(ns blog.auth.core
  (:use blog.template)
  (:use compojure.core)
  (:require [ring.util.response :as ring-response]))


; templates

(def login-template (templates "layout" "login"))


; helpers

(defn local-redirect [{server :server-name port :server-port} path]
  (ring-response/redirect (str "http://" server
                               (if (not (empty? (str port))) (str ":" port))
                               path)))


; server endpoints

(defn login [req]
  (println "login")
  {:body (login-template {})})

(defn process-login [{session :session params :params :as req}]
  (println "process")
  ; TODO check the received credentials
  (let [resp (local-redirect req "/")]
    (assoc-in resp [:session :logged-in] true)))


; authentication

(defn is-logged-in? [session]
  (:logged-in session))

(defmacro authenticated [req form]
  `(if-not (is-logged-in? (:session ~req))
    (local-redirect ~req "/login")
    ~form))


; routes

(defroutes auth-routes
  (GET  "/login" {:as req}
    (login req))
  (POST "/login" {:as req}
    (process-login req)))
