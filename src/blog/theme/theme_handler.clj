(ns blog.theme.theme-handler
  (:use [blog.handler :only [Handler]]
        [blog.theme.theme-handler-impl :only [handle-impl]]))

(defrecord ThemeHandler []
  Handler
   (handle [this req] ((var handle-impl) this req)))
