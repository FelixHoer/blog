(ns blog.auth.auth-handler
  (:use blog.handler
        [blog.auth.auth-handler-impl
         :only [start-impl stop-impl handle-impl]])
  (:require [com.stuartsierra.component :as component]))

(defrecord AuthHandler [static-resource-path db next]
  component/Lifecycle
    (start [this] ((var start-impl) this))
    (stop  [this] ((var stop-impl)  this))
  Handler
    (handle [this req] ((var handle-impl) this req)))
