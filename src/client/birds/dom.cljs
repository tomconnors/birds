(ns birds.dom
  "DOM utility functions"
  (:require [dommy.core :as dommy]))

(def body (.-body js/document))

(defn add-body-listener! [ev fun]
  (dommy/listen! body ev fun))

(defn remove-body-listener! [ev fun]
  (dommy/unlisten! body ev fun))

(defn clear! [el]
  (set! (.-innerHTML el) ""))

(defn kill-event [e]
  (.preventDefault e)
  (.stopPropagation e))

(defn kill-and-call [fun & args]
  (fn [e]
    (kill-event e)
    (apply fun args)))

(defn select-contents [el]
  (let [range (.createRange js/document)]
    (.selectNodeContents range el)
    (let [sel (.getSelection js/window)]
      (.removeAllRanges sel)
      (.addRange sel range))))

(defn checked? [el]
  (.-checked el))

(defn radio-group-value [elements]
  (dommy/value (first (filter checked? elements))))

(defn create-element [type]
  (.createElement js/document type))
