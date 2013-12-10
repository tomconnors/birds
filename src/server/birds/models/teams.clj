(ns birds.models.teams
  (:require [birds.util :as util]
            [birds.db :as db]
            [birds.shared.util :as shared-util]
            [birds.models.users :as users]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])))

(defn insert-team! [doc]
  (db/insert! :team (assoc doc :id (or (:id doc) (shared-util/unique-id)))))

(defn add-team-member! [team-id user-id access-level]
  (db/insert! :team-member {:team-id team-id
                            :user-id user-id
                            :access-level access-level}))

(defn remove-team-member! [team-id user-id]
  (db/delete! :team-member {:team-id team-id
                            :user-id user-id}))

(defn get-all-teams []
  (db/read :team {}))

(defn team-info [team-id]
  (let [team (db/read-one :team {:id team-id})
        team-members (db/read :team-member {:team-id (:id team)})
        members (map #(dissoc % :password) 
                     (map #(db/read-one :user {:id (:user-id %)}) team-members))
        members (map users/with-sightings members)]
    (assoc team
      :members members)))

(defn user-membership [team-id user-id]
  (let [membership (db/read-one :team-member {:team-id team-id
                                                  :user-id user-id})]
    (if membership (:access-level membership))))
