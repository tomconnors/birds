(ns birds.components.home
  (:require [pump :as pump]
            [react :as react]                      
            [birds.util :refer [log GET]]           
            [birds.history :as history]
            [birds.models.app :as state]
            [birds.models.team :as team]
            [birds.models.users :as user]
            [birds.components.misc :refer [Dialog]]
            [dommy.core :refer [listen! attr]]
            [birds.dom :as dom]
            [cljs.core.async :refer [chan <! >!]])
  (:require-macros [pump.macros :refer [defr] :as pump-macros]
                   [cljs.core.async.macros :refer [go]]
                   [dommy.macros :refer [sel1]]))



(defr NewTeamDialogBody
  :handle-submit (fn [c p s e]
                   (dom/kill-event e)
                   ((:on-submit p) (.-value (sel1 :#team-name)))
                   (.close c e))
  :close (fn [c p s e]
           (dom/kill-event e)
           ((:close p)))
  [component properties state]
  [:div
   [:form {:on-submit (.-handleSubmit component)}
    [:label {:for "team-name"} "Name"]
    [:input#team-name {:type "text" :name "name" :placeholder "name"}]
    [:br]
    [:input {:type "submit" :value "submit"}]
    [:input {:type "button" :value "cancel" :on-click (.-close component)}]e]])

(defr NewTeamDialog
  [component properties state]
  [Dialog (assoc properties
            :header (pump-macros/component [c p s] [:h4 "Make New Team"])
            :body NewTeamDialogBody
            :on-submit (fn [name] (team/new-team name)))])

(defr BirdDialogBody
  :handle-submit (fn [c p s e]
                   (dom/kill-event e)
                   ((:on-submit p)
                    (.-value (sel1 :#bird-type))
                    (.-value (sel1 :#count)))
                   (.close c e))
  :close (fn [c p s e]
           (dom/kill-event e)
           ((:close p)))
  [component properties state]
  [:div
   [:form {:on-submit (.-handleSubmit component)}
    [:label {:for "bird-type"} "Bird Type"]
    [:input#bird-type {:type "text" :name "type" :placeholder "Duck"}]
    [:br]
    [:label {:for "count"} "Number Sighted"]
    [:input#count {:type "number" :name "count"}]
    [:br]
    [:input {:type "submit" :value "submit"}]
    [:input {:type "button" :value "cancel" :on-click (.-close component)}]]])

(defr BirdDialog
  [components properties state]
  [Dialog (assoc properties
            :header (pump-macros/component [c p s] [:h4 "Add Bird Sighting"])
            :body BirdDialogBody
            :on-submit (fn [type count] (user/add-sighting type count)))])

(defr Home
  :get-initial-state (fn [& args] {:show-dialog false
                                   :show-bird-dialog false})
  :new-team (fn [c p s e]
              (dom/kill-event e)
              (swap! c assoc :show-team-dialog true))
  :new-sighting (fn [c p s e]
                  (dom/kill-event e)
                  (swap! c assoc :show-bird-dialog true))
  [component {:keys [user teams] :as properties} state]
  (log properties)
  [:div
   [:h2 "Teams"]
   (for [t teams]
     [:div
      [:a {:href (str "/team/" (:id t))} (:name t)]])
   (if user
     [:div
      [:h3
       [:a {:on-click (.-newTeam component) :href "#"} "New Team"]]
      [:h3
       [:a {:on-click (.-newSighting component) :href "#"} "Add Sighting"]]])
   (if (:show-team-dialog state)
     [NewTeamDialog (assoc properties
                      :close (fn [] (swap! component assoc :show-team-dialog false)))])
   (if (:show-bird-dialog state)
     [BirdDialog (assoc properties
                   :close (fn [] (swap! component assoc :show-bird-dialog false)))])])
