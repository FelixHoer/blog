(ns blog.theme.theme-handler
  (:require [com.stuartsierra.component :as component]
            [blog.handler :as handler]
            [blog.theme.theme-handler-impl :as impl]))

(defrecord ThemeHandler [template-resource-path static-resource-path ; config
                         templates static-routes] ; local data
  component/Lifecycle
    (start [this]
      (impl/start this))
    (stop [this]
      (impl/stop this))
  handler/Handler
    (wrap-handler [this next-handler]
      (impl/wrap-handler this next-handler)))
