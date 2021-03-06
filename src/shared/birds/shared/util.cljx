(ns birds.shared.util
  "Shared utility functions"
  (:require [clojure.string :as string]))
 

(defn apply-to-keys
  "apply fun to the keys of every key-value pair in the doc"
  [fun doc]
  (apply merge (map (fn [key-val] {(fun (first key-val)) (last key-val)})
                    doc)))

(defn parse-number
  "Reads a number from a string. Returns nil if not a number."
  [s]
  (if (re-find #"^-?\d+\.?\d*$" s)
    #+clj(read-string s) #+cljs(js/parseFloat s)))

(def not-nil? (complement nil?))

(defn boolean?
  "returns true if its arg is a boolean, false otherwise.
   How is there no standard lib fn for this?"
  [o] (or (true? o) (false? o)))

(let [i (atom 0)]
  (defn unique-id
    "Returns a distinct numeric ID for each call."
    []
    (swap! i inc)))

(defn diff-map
  "given arguments in groups of [key old new]
  create a map of {:key (new if (!= new old))}"
  [& args]
  (apply merge (map
                (fn [[key old new]]
                  (if-not (= old new)
                    {key new}))
                (partition 3 args))))

