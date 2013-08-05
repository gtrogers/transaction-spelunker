(ns transaction-spelunker.load_data
  (:require [clojure.string :as str]))

(defn slice-csv [csv]
  (let [lines (str/split csv #"\n")]
    ;#",(?=\")" this means only match commas followed by a speech mark
    (map (fn [line] (str/split line #",(?=\")")) lines)))

(defn keyify [col]
  (map (fn [s] (keyword (str/lower-case (str/replace s #"[\":\s]" "")))) col))

(defn try:string->dec [string]
  (let [result (try (bigdec string) (catch NumberFormatException e))
        value (if (str/blank? string) nil string)]
    (or result value)))

(defn valuefy [col]
  (map (fn [s] (try:string->dec (str/replace s #"\"" ""))) col))

(defn map-keys [key-value-col]
  (let [keys (keyify (first key-value-col))
        values (rest key-value-col)]
    (map (fn [value-row] (zipmap keys (valuefy value-row))) values)))
