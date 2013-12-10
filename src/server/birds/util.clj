(ns ^{:doc "Utilities that should eventually be given a more loving home."}
    birds.util
  (:require [clojure.string :as string]))

(defn log
  "Log all the args using pprint, and return the first arg."
  [& args]
  (doall (map clojure.pprint/pprint args))
  (first args))