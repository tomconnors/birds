(ns birds.routes.user
  "Routes for team pages, as well as routes for CRUD related to teams."
  (:require [birds.models.users :as users]
            [birds.models.teams :as teams]
            [birds.shared.models.teams :as shared-teams]
            [birds.shared.util :refer [parse-number] :as shared-util]
            [birds.util :refer [log]]
            [birds.routes.util :refer [with-request-params]]
            [compojure.core :refer [POST GET]]))

(defn data [team-id user]
  ;; respond with the data for this group
  ;; also include the current user's status in the group: nil,
  ;; :member, or :owner
  (let [current-user-team-status (teams/user-membership team-id (:id user))
        team (teams/team-info team-id)
        res {:team team
             :current-user-team-status current-user-team-status}]
    (if (= :owner current-user-team-status)
      (assoc res :users (users/get-all-users))
      res)))

(defn handle-req [{{id :id} :params} user] (data (parse-number id) user))

(defn add-sighting [type count user]
  (let [count (parse-number count)]
    (if (and user (number? count))
      (do
        (users/add-sighting! (:id user) type count)
        {:added true})
      {:failed true})))

(defn routes [route-handler]
  [(POST "/add-sighting" req (with-request-params add-sighting :type :count))])
