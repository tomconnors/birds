(ns birds.routes.home
  (:require [birds.models.users]
            [birds.models.teams :as teams]
            [birds.db :as db]
            [compojure.core :refer [GET POST]]))

(defn data [user]
  ;; return all bird teams
  {:teams (teams/get-all-teams)})

(defn handle-req [req user] (data user))

(defn routes [route-handler]
  [(GET "/" req (route-handler handle-req))])
