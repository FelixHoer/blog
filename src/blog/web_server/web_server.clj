(ns blog.web-server.web-server
  (:require [blog.web-server.web-server-impl :as impl]
            [com.stuartsierra.component :as component]))

(defrecord WebServer [port server]
  component/Lifecycle
    (start [this]
      (impl/start this))
    (stop [this]
      (impl/stop this)))
