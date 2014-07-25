(ns blog.auth.auth-handler-impl
  (:use compojure.core
        blog.handler
        blog.auth.auth-datastore)
  (:require [ring.util.response :as ring-response]
            [compojure.route :as route]))

; constants

(def LOGIN_SUCCESS_MSG "You logged in successfully!")
(def LOGIN_FAIL_MSG "Username and/or Password was incorrect!")
(def LOGOUT_SUCCESS_MSG "You logged out successfully!")


; helpers

(defn local-redirect [{scheme :scheme server :server-name port :server-port} path]
  (ring-response/redirect (str (name scheme) "://"
                               server
                               (if (seq (str port)) (str ":" port))
                               path)))

(defn is-logged-in? [session]
  (:logged-in session))

(defn is-logged-in-as? [session role]
  (and (:logged-in session)
       (= role
          (get-in session [:user :role]))))


; server endpoints

(defn login [req]
  {:template :login})

(defn process-login [{:keys [session form-params component resp] :as req}]
  (let [username (get form-params "username")
        password (get form-params "password")]
    (if-let [user (authenticate (:db component) username password)]
      (deep-merge (local-redirect req "/")
                  {:session {:logged-in true
                             :user user}
                   :data {:flash {:info LOGIN_SUCCESS_MSG}}})

      {:template :login
       :data {:flash {:warning LOGIN_FAIL_MSG}}})))

(defn process-logout [req]
  (deep-merge (local-redirect req "/")
              {:session nil
               :data {:flash {:info LOGOUT_SUCCESS_MSG}}}))

(defn enforce-auth [req]
  (local-redirect req "/login"))


; routes

(def auth-routes
  (routes
    (GET "/login" req
      (login req))
    (POST "/login" req
      (process-login req))
    (POST "/logout" req
      (process-logout req))))


; component

(defn start-impl [this]
  this)

(defn stop-impl [this]
  this)

(defn handle-impl [{next :next :as this} {session :session :as req}]
  (let [extended-req (assoc req :component this)]
    (if-let [auth-resp (auth-routes extended-req)]
      (update-in req [:resp] deep-merge auth-resp)
      (if-not (is-logged-in? session)
        (update-in req [:resp] deep-merge (enforce-auth extended-req))
        (if next
          (handle next req)
          req)))))
