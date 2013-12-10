(ns birds.history
  "Handle interacting with the history api.
   Built on top of history.js to smooth over cross-browser stupidity."
  (:require [birds.util :refer [log]]))

(defn push-state!
  "Add a state entry and update the url to show it."
  [url]
  (.pushState js/History nil nil url))

(defn get-state
  "Return the current route as a string"
  []
  (.-hash (.getState js/History)))

(defn on-state-change
  "Add a state-change callback. The callback should be a fn that takes a single
   'location' argument"
  [fun]
  (.bind (aget js/History "Adapter") js/window "statechange" #(fun (get-state))))




