(ns birds.models.users
  "Models for the collection of all users + the current user being viewed.
  The current user being viewed may or may not be the currently logged in user"
  (:require [clojure.set :refer [select]]
            [birds.util :refer [POST]]))


(defn add-sighting [type count]
  (POST "/add-sighting" :type type :count count))
