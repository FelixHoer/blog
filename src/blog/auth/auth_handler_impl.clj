(ns blog.auth.auth-handler-impl
  (:use compojure.core
        blog.handler
        blog.auth.auth-datastore)
  (:require [ring.util.response :as ring-response]
            [compojure.route :as route]))

; constants

(def LOGIN_SUCCESS_MSG "You logged in successfully!")
(def LOGIN_FAIL_MSG "Username and/or Password was incorrect!")


; helpers

(defn local-redirect [{server :server-name port :server-port} path]
  (ring-response/redirect (str "http://" server
                               (if (seq (str port)) (str ":" port))
                               path)))

(defn is-logged-in? [session]
  (:logged-in session))


; server endpoints

(defn login [req]
  {:template :login})

(defn process-login [{:keys [session params component resp] :as req}]
  (let [username (get params "username")
        password (get params "password")]
    (if (authenticate (:db component) username password)
      (deep-merge (local-redirect req "/")
                  {:session {:logged-in true
                             :username username}
                   :data {:flash {:info LOGIN_SUCCESS_MSG}}})

      {:template :login
       :data {:flash {:warning LOGIN_FAIL_MSG}}})))

(defn enforce-auth [req]
  (local-redirect req "/login"))


; routes

(def auth-routes
  (routes
    (GET "/login" {:as req}
      (login req))
    (POST "/login" {:as req}
      (process-login req))
    (ANY "*" {:as req}
      (enforce-auth req))))


; component

(defn start-impl [this]
  this)

(defn stop-impl [this]
  this)

(defn handle-impl [{next :next :as this} {session :session :as req}]
  (if-not (is-logged-in? session)
    (let [extended-req (assoc req :component this)
          resp (auth-routes extended-req)
          final-req (update-in req [:resp] deep-merge resp)]
      final-req)
    (if next
      (handle next req)
      req)))
