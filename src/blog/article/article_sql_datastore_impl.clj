(ns blog.article.article-sql-datastore-impl
  (:require [clojure.java.jdbc :as jdbc]
            [blog.article.helpers :as help]))


;;; helpers

(defn where-code [code]
  (let [{d :date t :title} (help/code->date+title {:code code})]
    ["UPPER(title) = UPPER(?) AND date = ?" t d]))


;;;; setup operations ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn create-article-table [{db :db}]
  (try
    (jdbc/db-do-commands db
      (jdbc/create-table-ddl :article
                             [:title "varchar(255)"]
                             [:date  "varchar(10)"]
                             [:body  "text"]))
    :ok
    (catch Exception e :fail)))


;;;; management operations ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; add article

(defn add-article [{db :db} article]
  (jdbc/insert! db :article (select-keys article [:title :date :body]))
  :ok)


;;; edit article

(defn edit-article [{db :db} code new-body]
  (jdbc/update! db :article {:body new-body} (where-code code))
  :ok)


;;; delete article

(defn delete-article [{db :db} code]
  (jdbc/delete! db :article (where-code code))
  :ok)


;;;; usage operations ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; paginated main list

(defn select-article-page [{:keys [db articles-per-page]} page-num]
  (jdbc/query db [(str "SELECT title, date, body "
                       "FROM article "
                       "ORDER BY date DESC "
                       "LIMIT ? "
                       "OFFSET ?")
                  articles-per-page
                  (* articles-per-page page-num)]
              :row-fn help/complete-article))

(defn select-article-count [{db :db}]
  (jdbc/query db [(str "SELECT COUNT(*) "
                       "FROM article")]
              :row-fn (comp second first vec)
              :result-set-fn first))

(defn article-page [{articles-per-page :articles-per-page :as this} page-num]
  (let [items (select-article-page this page-num)
        article-count (select-article-count this)
        has-next? (< (* (inc page-num) articles-per-page) article-count)
        has-previous? (pos? page-num)]
    (merge {:current-page page-num
            :items items}
           (if has-previous? {:previous-page (dec page-num)})
           (if has-next?     {:next-page     (inc page-num)}))))


;;; paginated month list

(defn select-article-month-page [{:keys [db articles-per-page]} page-num {:keys [month year]}]
  (jdbc/query db [(str "SELECT title, date, body "
                       "FROM article "
                       "WHERE CAST (EXTRACT(MONTH FROM CAST (date as DATE)) as INT) = CAST (? as INT) "
                       "  AND CAST (EXTRACT(YEAR  FROM CAST (date as DATE)) as INT) = CAST (? as INT) "
                       "ORDER BY date DESC "
                       "LIMIT ? "
                       "OFFSET ?")
                  month
                  year
                  articles-per-page
                  (* articles-per-page page-num)]
              :row-fn help/complete-article))

(defn select-article-month-count [{db :db} {:keys [month year]}]
  (jdbc/query db [(str "SELECT COUNT(*) "
                       "FROM article "
                       "WHERE CAST (EXTRACT(MONTH FROM CAST (date as DATE)) as INT) = CAST (? as INT) "
                       "  AND CAST (EXTRACT(YEAR  FROM CAST (date as DATE)) as INT) = CAST (? as INT)")
                  month
                  year]
              :row-fn (comp second first vec)
              :result-set-fn first))

(defn article-month-page [{articles-per-page :articles-per-page :as this} date page-num]
  (let [year+month (help/date->year+month+day {:date date})
        items (select-article-month-page this page-num year+month)
        article-count (select-article-month-count this year+month)
        has-next? (< (* (inc page-num) articles-per-page) article-count)
        has-previous? (pos? page-num)]
    (merge {:current-page page-num
            :items items}
           (if has-previous? {:previous-page (dec page-num)})
           (if has-next?     {:next-page     (inc page-num)}))))


;;; single article

(defn select-article [{db :db} code]
  (let [[code-clause & params] (where-code code)]
    (jdbc/query db (concat [(str "SELECT title, date, body "
                                 "FROM article "
                                 "WHERE " code-clause)]
                           params)
                :row-fn help/complete-article)))

(defn article [this code]
  {:items (select-article this code)
   :current-page 0})


;;; article overview

(defn select-recent-articles [{db :db recent-articles :recent-articles}]
  (jdbc/query db [(str "SELECT title, date "
                       "FROM article "
                       "ORDER BY date DESC "
                       "LIMIT ?")
                  recent-articles]
              :row-fn help/complete-article))

(defn parse-article-date [{m :month y :year}]
  (help/complete-article {:month (format "%02d" m)
                          :year  (format "%4d"  y)}))

(defn select-archive-months [{db :db}]
  (jdbc/query db [(str "SELECT DISTINCT CAST (EXTRACT(MONTH FROM CAST (date as DATE)) as INT) as month, "
                       "                CAST (EXTRACT(YEAR  FROM CAST (date as DATE)) as INT) as year "
                       "FROM article "
                       "ORDER BY year, month DESC")]
              :row-fn parse-article-date))

(defn article-overview [this]
  {:recent-articles (select-recent-articles this)
   :archive-months (select-archive-months this)})

