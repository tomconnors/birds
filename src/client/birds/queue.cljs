(ns birds.queue
  "A (probably terrible) implementation of observable queues.
  Really just an attempt at pub-sub with core.async."
  (:require [cljs.core.async :as async :refer
             [chan <! >! timeout sliding-buffer take!]]
            [cljs.core.async.impl.protocols :as proto]
            [birds.util :refer [log put-all!]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(defprotocol IObservable
  (subscribe [c observer] [c observer pred])
  (unsubscribe [c observer]))

(defprotocol IWriteable
  (put! [c val]))

(defn observable-queue
  "create a new observable queue from the channel 'c'.
  Observers may supply a predicate to limit the values that they are alerted about."
  [c]
  (let [listeners (atom #{})]
    (go (loop [] 
          (let [val (<! c)]
                (when val
                  (put-all! (map :ch (filter
                                      (fn [{pred :pred}]
                                        (pred val))
                                      @listeners)) val)
                  (recur)))))
    (reify
      IWriteable
      (put! [_ val] (go (>! c val)))
      IObservable
      (subscribe
        [this observer] (do (swap! listeners conj {:ch observer
                                                   :pred (constantly true)})
                            observer))
      (subscribe
        [this observer pred] (do (swap! listeners conj {:ch observer
                                                        :pred pred})
                                 observer))
      (unsubscribe [this observer]
        (swap! listeners (fn [list] (remove (fn [{ch :ch}] (= ch observer)) list)))
        observer))))
