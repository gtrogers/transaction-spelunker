(ns transaction-spelunker.core
  (:import [java.math BigDecimal])
  (:require [transaction-spelunker.load_data :as load-data]
            [transaction-spelunker.transaction_explorer :as te]
            [transaction-spelunker.html_generators :as make-a]
            [clojure.pprint :as pp]))

(defn -main []
  (let [tdata (load-data/map-keys (load-data/slice-csv (slurp "data/transaction-volumes.csv")))
        departments (te/departments tdata)]
    (print
      (make-a/table
        ["Department" "Digital take-up" "Total cost" "Data coverage" "Transactions per year"]
        (map (fn [dept] (conj [dept "?" "?" "?"] "+")) (partition 1 1 departments))))))