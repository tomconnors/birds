(ns birds.models.team
  (:require [birds.models.app :as state]
            [birds.util :refer [POST log]]
            [birds.history :as history]
            [cljs.core.async :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn new-team [name]
  (go
   (let [[ok res] (<! (POST "/new-team" :name name))]
     (when ok
       (state/put-update! [] res)
       (history/push-state! (str "/team/" (get-in res [:team :id])))))))

(defn remove-team-member-trans [target op old-val app-state {id :id}]
  (remove #(= (:id %) id) old-val))

(defn remove-team-member-effect [target op  app-state {id :id}]
  [:remove-team-member :user-id id :team-id (get-in app-state [:team :id])])

(defn add-team-member-trans [_ _ old-val _ {user :user}] 
  (conj old-val (assoc user :sightings [])))

(defn add-team-member-effect [t o {{team-id :id} :team :as s} {{user-id :id} :user}]
  (go (let [[ok res] (<! (POST "/add-team-member"
                               :team-id team-id
                               :user-id user-id))]
        (if ok
          (state/put-update! [] res)))))

(defn init []
  
  (state/register-transform! [:team :members] :remove remove-team-member-trans)
  (state/register-transform! [:team :members] :add add-team-member-trans)
  
  (state/register-effect! [:team :members]
                          :remove
                          (state/default-effect remove-team-member-effect))
  (state/register-effect! [:team :members] :add add-team-member-effect))

