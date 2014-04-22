(ns blog.auth.auth-handler-impl
  (:use compojure.core
        blog.handler)
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

(defn handle-impl [{:keys [auth-routes next final] :as this} {session :session :as req}]
  (if (is-logged-in? session)
    (handle next req)
    (let [extended-req (assoc req :component this)
          resp (auth-routes extended-req)
          final-req (update-in req [:resp] merge resp)]
      (handle final final-req))))
