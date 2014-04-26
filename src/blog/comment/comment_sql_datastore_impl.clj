(ns blog.comment.comment-sql-datastore-impl)


; component

(defn save-comment-impl [this comment article-code]
  (println "save-comment-impl" comment article-code))

(defn read-comment-counts-impl [this article-codes]
  (println "read-comment-counts-impl" article-codes)
  (into {} (map (fn [c] [c 3]) article-codes)))

(defn read-comments-impl [this article-code]
  (println "read-comments-impl" article-code)
  [{:name "some user" :time (java.util.Date.) :text "lorem ipsum"}
   {:name "some user1" :time (java.util.Date.) :text "loremasfd ipsum"}
   {:name "some user3" :time (java.util.Date.) :text "lorem fsfsipsum"}])
