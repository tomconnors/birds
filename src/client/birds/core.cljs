(ns birds.core
  (:require [cljs.core.async :as async :refer
             [chan <! >! timeout sliding-buffer put! take!]]
            [cljs.core.async.impl.protocols :as proto]
            [birds.util :refer [log]]           
            [pump :as pump]
            [cljs.reader :refer [read-string]]
            [birds.queue :refer [observable-queue] :as oqueue]           
            [birds.models.app :as app-state]
            [birds.models.team :as team]
            [birds.history :as history]
            
            ;; here so the ns is available for the brepl 
            [clojure.browser.repl]
            [birds.components.app :as layout])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]
                   [pump.macros :refer [defr]]))

(def init-data-el (.querySelector js/document "#init-data"))

(defn init 
  "Initialize the application by calling init on anything that needs initialization,
  then putting the initial data into the app-state.
  Hint - the layout sees that this occurred (on the output queue),
  then starts rendering the application."
  [initial-data]
  (layout/init)
  (team/init)
  (app-state/init)
  (app-state/put-update! [] initial-data))

;;Read the initial data from the script tag on the page #init-data.
;; The data in the script is edn that was written to the page by the server.
;; This avoids using an ajax request to get the initial data.
(let [bootstrap-data (read-string (.-innerText init-data-el))
      init-data (merge {:route (history/get-state)} bootstrap-data)]
  ;; set everything in motion.
  (init init-data))
