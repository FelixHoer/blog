(ns blog.auth.auth-handler-impl
  (:use compojure.core
        blog.handler
        blog.auth.auth-datastore)
  (:require [ring.util.response :as ring-response]
            [compojure.route :as route]))


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

(defn process-login [{:keys [session params component] :as req}]
  (let [username (get params "username")
        password (get params "password")]
    (if (authenticate (:db component) username password)
      (let [resp (local-redirect req "/")]
        (update-in resp [:session] merge {:logged-in true
                                          :username username}))
      {:template :login
       :data {:warning "Username and/or Password was incorrect!"}})))

(defn enforce-auth [req]
  (local-redirect req "/login"))


; routes

(defn setup-auth-routes [static-resource-path]
  (routes
    (GET "/login" {:as req}
      (login req))
    (POST "/login" {:as req}
      (process-login req))
    (route/resources "/" {:root static-resource-path})
    (ANY "*" {:as req}
      (enforce-auth req))))


; component

(defn start-impl [{static-resource-path :static-resource-path :as this}]
  (assoc this :auth-routes (setup-auth-routes static-resource-path)))

(defn stop-impl [this]
  this)

(defn handle-impl [{:keys [auth-routes next] :as this}
                   {session :session :as req}]
  (if-not (is-logged-in? session)
    (let [extended-req (assoc req :component this)
          resp (auth-routes extended-req)
          final-req (update-in req [:resp] merge resp)]
      final-req)
    (if next
      (handle next req)
      req)))
