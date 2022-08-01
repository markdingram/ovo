(ns analyse
  (:require
   [cheshire.core :as cheshire]
   [clojure.data.csv :as csv]
   [clojure.java.io :as io]
   [java-time :as time]))

; analyzes the data that was retrieved by the 'fetch' ns - a series of JSON files for each date in data/YYYY-MM-DD.json


(defn load-day [day]
  (binding [cheshire.parse/*use-bigdecimals?* true]
    (cheshire/parse-stream (io/reader (str "data/" day ".json")) true)))


(comment
  ;; check we can load a single day
  (let [elec (-> (load-day (time/local-date 2022 1 2))
                 :electricity
                 :data)]
    elec))


(defn to-uk-zdt
  "Converts ISO Local Date Time string to UK time"
  [time]
  (let [formatter (time/formatter :iso-local-date-time)]
    (time/zoned-date-time (time/local-date-time formatter time) "Europe/London")))


;;
;; MPAN 1100...
;; https://forum.ovoenergy.com/my%2Daccount%2D140/everything%2Dyou%2Dneed%2Dto%2Dknow%2Dabout%2Dan%2Deconomy%2D7%2Deco%2D7%2Denergy%2Dplan%2D6455
;; Off peak hour 00:00 to 07:00
;; Aclara doesn't adjust for BST so then it is 1:00 to 8:00am


(defn classify-peak-or-off-peak
  "Aclara doesn't adjust for BST, i.e. off peak times are 0-7am UTC all year round"
  [zdt]
  (let [hour (-> zdt
                 (time/with-zone-same-instant "UTC")
                 (time/as :hour-of-day))]
    (if (< hour 7) :off-peak :peak)))

(comment
  ;; meter readings are UK Time
  (to-uk-zdt "2022-01-13T00:00:00.000")
  (to-uk-zdt "2022-06-13T00:00:00.000")

  (time/with-zone-same-instant (to-uk-zdt "2022-06-13T00:00:00.000") "UTC")

  (classify-peak-or-off-peak (to-uk-zdt "2022-01-13T00:00:00.000"))
  (classify-peak-or-off-peak (to-uk-zdt "2022-01-13T07:30:00.000"))
  (classify-peak-or-off-peak (to-uk-zdt "2022-01-13T08:00:00.000"))
  (classify-peak-or-off-peak (to-uk-zdt "2022-06-13T23:00:00.000"))
  (classify-peak-or-off-peak (to-uk-zdt "2022-06-13T00:30:00.000"))
  (classify-peak-or-off-peak (to-uk-zdt "2022-06-13T01:00:00.000"))
  (classify-peak-or-off-peak (to-uk-zdt "2022-06-13T07:00:00.000"))
  (classify-peak-or-off-peak (to-uk-zdt "2022-06-13T08:00:00.000")))


(defn classify-date
  "group the days electricity reading into peak or off-peak"
  [date]
  (let [elec (-> (load-day date)
                 :electricity
                 :data)]
    (->
     (group-by (comp classify-peak-or-off-peak second)
               (map (juxt :consumption #(to-uk-zdt (get-in % [:interval :start]))) elec))
     (assoc :date date))))

(defn usage-per-date
  "Sums usage per date"
  [classified-date]
  (-> classified-date
      (update :off-peak #(reduce + (map first %)))
      (update :peak #(reduce + (map first %)))))


(comment
  (classify-date (time/local-date 2022 1 2))

  (-> (time/local-date 2022 1 2)
      (classify-date)
      (usage-per-date)))


(defn date-range
  [start-date end-date]
  "returns dates between start and end (inclusive)"
  (let [dates-from-start (time/iterate time/plus start-date (time/days 1))]
    (take-while #(time/before? % (time/plus end-date (time/days 1))) dates-from-start)))


(comment
  (date-range (time/local-date 2022 1 2) (time/local-date 2022 1 21))

  ;; off peak stopped submission from 16th May until end of the tariff
  (let [dates (date-range (time/local-date 2021 5 16) (time/local-date 2022 2 17))]
    (map (comp usage-per-date classify-date) dates)))


(comment
  (let [dates (date-range (time/local-date 2021 5 16) (time/local-date 2022 2 17))
        usage (map (comp usage-per-date classify-date) dates)]
    (with-open [writer (java.io.FileWriter. "data/results.csv")]
      (csv/write-csv writer [["date" "off-peak" "peak"]])
      (csv/write-csv writer (map #((juxt :date :off-peak :peak) %) usage)))))
