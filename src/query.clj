(ns query
  (:require
   [cheshire.core :as cheshire]
   [java-time :as time]
   [clj-http.client :as client]
   clj-http.cookies))


(def cookies (clj-http.cookies/cookie-store))

(def account-id (System/getenv "OVO_ACCOUNT_ID"))
(def username (System/getenv "OVO_USERNAME"))
(def password (System/getenv "OVO_PASSWORD"))

(defn login []
  (client/post "https://my.ovoenergy.com/api/v2/auth/login" {:content-type :json
                                                             :form-params {:username username
                                                                           :password password
                                                                           :rememberMe true}
                                                             :cookie-policy :standard
                                                             :cookie-store cookies}))

(defn get-day [day]
  (let [resp (client/get (str "https://smartpaym.ovoenergy.com/api/energy-usage/half-hourly/" account-id "?date=" day)
                         {:as :json
                          :cookie-policy :standard
                          :cookie-store cookies})
        body (:body resp)
        json (cheshire/generate-string body {:pretty true})]
    json))


(defn save-day [day json]
  (spit (str "data/" day ".json") json))

(comment
  (let [days (time/iterate time/plus (time/local-date 2022 1 1) (time/days 1))]
    (map str (take 5 days))))

(comment
  (login)
  ;(let [day (time/local-date 2022 1 2)]
  ;  (save-day day (get-day day)))
  (let [first-day (time/local-date 2021 3 24)
        last-day (time/local-date 2022 7 01)
        no-days (time/time-between first-day last-day :days)
        all-days (time/iterate time/plus first-day (time/days 1))]
    (doseq [day (take no-days all-days)]
      (println day)
      (save-day day (get-day day)))))
  
