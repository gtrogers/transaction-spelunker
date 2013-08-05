(ns transaction-spelunker.transaction_explorer)

(defn departments [data]
  (set (map :department data)))

(defn nil+ [mem n]
  (if n (+ n mem) mem))

(defn vals-for-dept [dept key data]
  (let [data-for-dept (filter (fn [d] (= (d :department ) dept)) data)]
    (map (fn [d] (get d key)) data-for-dept)))

(defn reduce-with-fallback [dept try-this-key then-this-key data]
  (let [dept-data (filter (fn [d] (= (d :department ) dept)) data)]
    (reduce nil+ (map (fn [d] (or (get d try-this-key) (get d then-this-key))) dept-data))))

(defn digital-take-up [dept data]
  (let [total-volume (reduce-with-fallback dept :2013q1volume :2012q4volume data)
        digital-volume (reduce-with-fallback dept :2013q1digitalvolume :2012q4digitalvolume data)]
    (* 100 (with-precision 3 (/ digital-volume total-volume)))))

(defn do-for-dept [dept do-this data]
  (let [dept-data (filter (fn [d] (= (d :department ) dept)) data)]
    (map do-this dept-data)))

(defn transactions-per-year [dept data]
  (let [no-of-transactions (do-for-dept dept (fn [d] (or (d :2013q1volume) (d :2012q4volume))) data)]
    (reduce nil+ no-of-transactions)))