(ns birds.models.users
  (:require [birds.util :as util]
            [birds.shared.util :as shared-util]
            [birds.db :as db]
            [birds.shared.models.users]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])))

(defn free-username? [name] true)

(defn insert-user! [user]
  (db/insert! :user (assoc user :id (shared-util/unique-id))))

(defn find-user-by-name [name]
  (db/read-one :user {:name name}))

(defn get-all-users []
  (map #(dissoc % :password) (db/read :user {})))

(defn add-sighting! [user-id bird-type count]
  (db/insert! :sighting {:user-id user-id
                        :bird-type bird-type
                        :count count}))

(defn with-sightings [{id :id :as user}]
  (assoc user :sightings (db/read :sighting {:user-id id})))
