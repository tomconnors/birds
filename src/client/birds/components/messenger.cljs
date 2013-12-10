(ns birds.components.messenger
  (:require [pump :as pump]
            [react :as react]
            [birds.util :refer [log]]
            [birds.dom :refer [kill-event]]
            [birds.models.app :as state]
            [birds.shared.util :refer [unique-id]]
            [dommy.core :refer [listen! attr]]
            [cljs.core.async :refer [chan <! >!]])
  (:require-macros [pump.macros :refer [defr]]
                   [cljs.core.async.macros :refer [go]]))

(def ^{:private true} messages (atom []))

(defr Message
  :close (fn [c p s] (swap! messages
                            (fn [list id]
                              (remove #(= (:id id))
                                      list))
                            (:id p)))
  :component-did-mount (fn [c p s]
                         (if (:duration p)
                           (js/setTimeout #(.close c) (* 1000 (:duration p)))))
  [c p s]
  [:div.flash-message
   [:i.fa.fa-times.flash-message__close {:on-click (.-close c)}]
   [:p (if-let [component (:component p)] component (:text p))]])

(defr Messenger
  :get-initial-state (fn [c p s] {:messages @messages})
  :component-will-mount (fn [c p s] (add-watch messages
                                               :msg-change
                                               (fn [_ _ _ new]                             
                                                 (.replaceState c {:messages new}))))
  :component-will-unmount (fn [c p s] (remove-watch messages :msg-change))
  [component props {ms :messages :as s}]
  (if (> (count ms) 0)
    [:div.flash-messages
     (for [m ms]
       [Message m])]
    [:span.hidden]))

(defn message [{:keys [text duration component] :as options}]
  (swap! messages conj (assoc options :id (unique-id))))
