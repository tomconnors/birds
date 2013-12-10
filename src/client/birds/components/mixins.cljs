(ns birds.components.mixins
  (:require [birds.util :refer [toggle-bool defer log]]
            [birds.dom :refer [add-body-listener! remove-body-listener!]]))

(extend-protocol ILookup
  object
  (-lookup [m k] (aget m k))
  (-lookup [m k not-found] (or (aget m k) not-found)))

(defn hide-on-external-event [event external-event-handler]
  {:internal-event (fn [c _ _ _]                    
                     (let [{show :show} (swap! c toggle-bool :show)]
                       (if show
                         (defer
                           #(add-body-listener! event (get c external-event-handler)))
                         (remove-body-listener! event (get c external-event-handler)))))
   :external-event (fn [c _ _ e]                 
                     (when (not (.contains (.getDOMNode c) (.-target e)))
                       (swap! c assoc :show false)
                       (remove-body-listener! event (get c external-event-handler))))
   :component-will-unmount (fn [c _ _ _]
                             ;; todo: how to verify this actually works?
                             (when (:show (.-state c))
                               (remove-body-listener! event
                                                      (get c external-event-handler))))})
