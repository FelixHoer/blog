(ns blog.theme.theme-handler
  (:use [blog.handler :only [Handler]]
        [blog.theme.theme-handler-impl :only [start-impl stop-impl handle-impl]])
  (:require [com.stuartsierra.component :as component]))

(defrecord ThemeHandler [template-resource-path static-resource-path
                         templates static-routes]
  component/Lifecycle
    (start [this] ((var start-impl) this))
    (stop  [this] ((var stop-impl)  this))
  Handler
    (handle [this req] ((var handle-impl) this req)))
