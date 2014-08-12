(ns blog.auth.auth-handler-impl
  (:require [ring.util.response :as ring]
            [compojure.route :as route]
            [blog.handler :as handler]
            [blog.auth.auth-datastore :as ds]
            [compojure.core :refer [defroutes GET POST]]
            [validateur.validation :as v]))

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


;;; validation

(def login-validator
  (v/validation-set
   (v/presence-of :username)
   (v/length-of   :username :within (range 1 100))
   (v/presence-of :password)
   (v/length-of   :password :within (range 1 100))))


;;; server endpoints

(defn show-login [req & [data]]
  (let [resp {:template :login}]
    (if data
      (assoc resp :data data)
      resp)))

(defn process-login [{{u "username" p "password"} :form-params
                         {db :db} :component
                         :as req}]
  (let [input {:username u}
        validate #(let [errors (login-validator (assoc input :password p))]
                    (if-not (v/valid? errors)
                      (show-login req {:user input
                                       :errors errors})))
        process  #(if-let [user (ds/authenticate db u p)]
                    (handler/deep-merge (local-redirect req "/")
                                        {:session {:logged-in true
                                                   :user user}
                                         :data {:flash {:info LOGIN_SUCCESS_MSG}}})
                    (show-login req {:user input
                                     :flash {:warning LOGIN_FAIL_MSG}}))]
    (some #(%) [validate process])))

(defn process-logout [req]
  (handler/deep-merge (local-redirect req "/")
                      {:session nil
                       :data {:flash {:info LOGOUT_SUCCESS_MSG}}}))

(defn enforce-auth [req]
  (local-redirect req "/login"))


;;; routes

(defroutes auth-routes
  (GET "/login" req
    (show-login req))
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
