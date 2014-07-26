(ns blog.auth.auth-handler-impl
  (:require [ring.util.response :as ring]
            [compojure.route :as route]
            [blog.handler :as handler]
            [blog.auth.auth-datastore :as ds]
            [compojure.core :refer [defroutes GET POST]]))

;;; constants

(def LOGIN_SUCCESS_MSG "You logged in successfully!")
(def LOGIN_FAIL_MSG "Username and/or Password was incorrect!")
(def LOGOUT_SUCCESS_MSG "You logged out successfully!")


;;; helpers

(defn local-redirect [{scheme :scheme server :server-name port :server-port} path]
  (ring/redirect (str (name scheme) "://"
                      server
                      (if (seq (str port)) (str ":" port))
                      path)))

(defn is-logged-in? [session]
  (:logged-in session))

(defn is-logged-in-as? [session role]
  (and (:logged-in session)
       (= role
          (get-in session [:user :role]))))


;;; server endpoints

(defn login [req]
  {:template :login})

(defn process-login [{:keys [session form-params component resp] :as req}]
  (let [username (get form-params "username")
        password (get form-params "password")]
    (if-let [user (ds/authenticate (:db component) username password)]
      (handler/deep-merge (local-redirect req "/")
                          {:session {:logged-in true
                                     :user user}
                           :data {:flash {:info LOGIN_SUCCESS_MSG}}})

      {:template :login
       :data {:flash {:warning LOGIN_FAIL_MSG}}})))

(defn process-logout [req]
  (handler/deep-merge (local-redirect req "/")
                      {:session nil
                       :data {:flash {:info LOGOUT_SUCCESS_MSG}}}))

(defn enforce-auth [req]
  (local-redirect req "/login"))


;;; routes

(defroutes auth-routes
  (GET "/login" req
    (login req))
  (POST "/login" req
    (process-login req))
  (POST "/logout" req
    (process-logout req)))


;;; component

(defn handle [this next-handler {session :session resp :resp :as req}]
  (let [extended-req (assoc req :component this)]
    (if-let [auth-resp (auth-routes extended-req)]
      (handler/deep-merge resp auth-resp)
      (if-not (is-logged-in? session)
        (handler/deep-merge resp (enforce-auth extended-req))
        (next-handler req)))))

(defn wrap-handler [this next-handler]
  #(handle this next-handler %))
