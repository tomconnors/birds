(ns birds.models.dev-data
  (:require [birds.db :as db]
            [birds.models.users :as users]
            [birds.models.teams :as teams]
            [birds.shared.util :as util]
            [cemerick.friend.credentials :as creds]))

(defn add-fixture-data []
  ;;users 
  (users/insert-user! {:name "bill"                      
                       :password (creds/hash-bcrypt "123456")})
  (users/insert-user! {:name "phil"
                       :password (creds/hash-bcrypt "123456")})
  (users/insert-user! {:name "jill"
                       :password (creds/hash-bcrypt "123456")})

  ;; teams
  (teams/insert-team! {:name "The Great Birdfolk"})
  (teams/insert-team! {:name "The Birdwhisperers"})

  ;; team-members
  (teams/add-team-member! (:id (db/read-one :team {:name "The Great Birdfolk"}))
                            (:id (db/read-one :user {:name "bill"}))
                            :owner)

  (users/add-sighting! (:id (db/read-one :user {:name "bill"}))
                       "canary"
                       3))

#_ (db/clear-all!)
#_(add-fixture-data)
