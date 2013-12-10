(ns birds.routes.team
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

(defn add-team-member [team-id user-id user]
  (when (= :owner (teams/user-membership team-id (:id user)))
    (teams/add-team-member! team-id user-id :member)
    (data team-id user)))

(defn remove-team-member [team-id user-id user]
  (when (= :owner (teams/user-membership team-id (:id user)))
    (teams/remove-team-member! team-id user-id)
    {:removed true}))

(defn add-team [team-name user]
  (when user
    (let [id (shared-util/unique-id)]
      (teams/insert-team! {:name team-name
                           :id id})
      (teams/add-team-member! id (:id user) :owner)
      (str (data id user)))))

(defn routes [route-handler]
  [(POST "/add-team-member" req (with-request-params add-team-member :team-id :user-id))
   (POST "/remove-team-member" req
         (with-request-params remove-team-member :team-id :user-id) )
   (POST "/new-team" req (with-request-params add-team :name))
   (GET "/team/:id" req (route-handler handle-req))])
