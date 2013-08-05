(ns transaction-spelunker.core-test
  (:require [clojure.test :refer :all ]
            [transaction-spelunker.load_data :refer :all ]
            [transaction-spelunker.transaction_explorer :refer :all ]))

;test helper which replaces single quotes with double quotes
(defn dq [string]
  (clojure.string/replace string #"'" "\""))

;test helper which replaces single quotes with double quotes for a collections
(defn dqc [col]
  (map dq col))

; tests for src/load_data.clj

(deftest double-quoting-a-string
  (testing "replaces single quotes with escaped double quotes so I don't go mad with string escapes"
    (is (= "\"a\"" (dq "'a'")))
    (is (= ["\"a\"" "\"b\""] (dqc ["'a'" "'b'"])))))

(deftest slicing-a-csv
  (testing "Splitting a csv into vectors"
    (is (= (slice-csv (dq "'a','b','c'\n'd','e','f'"))
          [(dqc ["'a'" "'b'" "'c'"]) (dqc ["'d'" "'e'" "'f'"])]))))

(deftest slicing-a-csv-with-extra-commas
  (testing "Splitting a csv should preserve commas inside speech marks"
    (is (= (slice-csv (dq "'a,b','c'\n'd','e,f'"))
          [(dqc ["'a,b'" "'c'"]) (dqc ["'d'" "'e,f'"])]))))

(deftest slicing-a-csv-with-empty-bits
  (testing "Spltting a csv should preserve empty fields"
    (is (= (slice-csv (dq "'a','','c'"))
          [(dqc ["'a'" "''" "'c'"])]))))

(deftest converting-row-to-keys
  (testing "Should convert quoted strings to keywords removing junk from the strings"
    (is (= (keyify (dqc ["'key1'" "'key2'" "'2013 Q1: something'"])) [:key1 :key2 :2013q1something ]))))

(deftest converting-row-to-values
  (testing "Should remove quotes from beggining and end of values"
    (is (= (valuefy (dqc ["'val'" "'another val'"])) ["val" "another val"]))))

(deftest converting-value-strings-to-decimals-if-appropriate
  (testing "Should convert string to decimal if it is a number"
    (is (= (try:string->dec "15") 15M))
    (is (= (try:string->dec "0.5") 0.5M))
    (is (= (try:string->dec "Hello") "Hello"))
    (is (= (try:string->dec "15 Hello 12") "15 Hello 12"))))

(deftest converting-empty-value-strings-to-nil
  (testing "Should convert empty strings to nil"
    (is (nil? (try:string->dec "")))))

(deftest converting-row-with-numbers-to-values
  (testing "Should parse out the numbers"
    (is (= (valuefy (dqc ["'val'" "'another val'" "'12345'" "'0.5'"])) ["val" "another val" 12345M 0.5M]))))

(deftest wouldnt-it-be-nice-if-empty-strings-were-nilsy
  (testing "Should convert empty stirng values to nil"
    (is (= (valuefy (dqc ["'val'" "''" "''" "'5'"])) ["val" nil nil 5M]))))

(deftest mapping-keys-to-values
  (testing "Should map keys to values"
    (is (= (map-keys [(dqc ["'key1'" "'key2'" "'key3'"])
                      (dqc ["'a'" "'b'" "'c is a bit longer'"])
                      (dqc ["'x'" "'y'" "'z'"])])
          [{:key1 "a", :key2 "b", :key3 "c is a bit longer"}
           {:key1 "x", :key2 "y", :key3 "z"}]))))

; tests for src/transaction_explorer.clj

(deftest nil-safe-addition
  (testing "reducing a coll with nils should ignore the nils"
    (is (= (reduce nil+ [1 2 3 nil 4])
            10))))

(deftest list-of-departments
  (testing "Getting a list of all departments in the data"
    (is (= (departments [{:department "Foo"} {:department "Foo"} {:department "Bar"} {:department "Zap"}])
          #{"Foo" "Zap" "Bar"}))))

(deftest get-all-values-for-department
  (testing "Getting all of the values of a key for a department"
    (is (= (vals-for-dept "B" :key [{:department "A", :key 5} {:department "B", :key 7} {:department "B", :key 3}])
          [7 3]))))

(deftest calculating-most-recent-volume
  (testing "Getting the data for 2013q1 or falling back to the data for 2012q4"
    (is (= (reduce-with-fallback "A" :2013q1volume :2012q4volume [{:department "A", :2012q4volume 2000M, :2013q1volume 2500M}
                                                                  {:department "A", :2012q4volume 1500M, :2013q1volume nil}
                                                                  {:department "B", :2012q4volume 1000M, :2013q1volume nil}])
          4000M))))

(deftest finding-digital-takeup
  (testing "Digital take up should = digital volume / total volume"
    (is (= (digital-take-up "A" [{:department "A", :2012q4volume 100M, :2013q1volume 150M, :2012q4digitalvolume 50M}
                                 {:department "A", :2012q4volume 100M, :2013q1volume 150M, :2012q4digitalvolume 50M}
                                 {:department "A"}])
          33.300M))))

(deftest do-for-all-of-a-department
  (testing "Return the result of a function across all of a given depertment"
    (is (= (do-for-dept "Foo" (fn [d] (d :key)) [{:department "Foo", :key 1}
                                                 {:department "Foo", :key 2}
                                                 {:department "Bar", :key 3}])
           [1 2]))))

(deftest transactions-per-year-calculation
  (testing "calculating transactions per year"
    (is (= (transactions-per-year "A" [{:department "A", :2012q4volume 10M}
                                   {:department "A", :2013q1volume 20M}
                                   {:department "A", :2012q4volume 5M, :2013q1volume 3M}
                                   {:department "A"}])
          33M))))
