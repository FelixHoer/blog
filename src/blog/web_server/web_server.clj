(ns blog.web-server.web-server
  (:use [blog.web-server.web-server-impl :onld [start-impl stop-impl]])
  (:require [com.stuartsierra.component :as component]))

(defrecord WebServer [port next jetty]
  component/Lifecycle
  (start [this] ((var start-impl) this))
  (stop  [this] ((var stop-impl)  this)))
