(ns blog.handler)

(defn deep-merge
  "Recursively merges maps. If keys are not maps, the last value wins."
  [& vals-with-nil]
  (letfn [(merge-internal [& vals] (if (every? map? vals)
                                     (apply merge-with merge-internal vals)
                                     (last vals)))]
    (apply merge-internal (remove nil? vals-with-nil))))

(defn deep-merge-in [m path & vals]
  (apply update-in m path deep-merge vals))

(defprotocol Handler
  (wrap-handler [this next-handler]))
