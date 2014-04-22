(ns blog.auth.auth-handler-impl
  (:use compojure.core
        blog.handler
        blog.constants)
  (:require [ring.util.response :as ring-response]
            [compojure.route :as route]))


; helpers

(defn local-redirect [{server :server-name port :server-port} path]
  (ring-response/redirect (str "http://" server
                               (if (not (empty? (str port))) (str ":" port))
                               path)))

(defn is-logged-in? [session]
  (:logged-in session))


; server endpoints

(defn login [req]
  {:template :login})

(defn process-login [{session :session params :params :as req}]
  ; TODO check the received credentials
  (let [resp (local-redirect req "/")]
    (assoc-in resp [:session :logged-in] true)))

(defn enforce-auth [req]
  (local-redirect req "/login"))


; routes

(defroutes auth-routes
  (GET "/login" {:as req}
    (login req))
  (POST "/login" {:as req}
    (process-login req))
  (route/resources "/" {:root STATIC_RESOURCE_PATH})
  (ANY "*" {:as req}
    (enforce-auth req)))


; component

(defn start-impl [this]
  this)

(defn stop-impl [this]
  this)

(defn handle-impl [this {session :session :as req}]
  (if (is-logged-in? session)
    (handle (:next this) req)
    (let [extended-req (assoc req :component this)
          resp (auth-routes extended-req)
          next (:final this)
          next-req (update-in req [:resp] merge resp)]
      (handle next next-req))))
