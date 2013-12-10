(ns birds.auth
  (:require [birds.util :refer [log POST]]
            [birds.models.app :as app-state]
            [cljs.core.async :refer [<! >!]]
            [birds.history :as history])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn login
  "Attempt to log in with name and password"
  [name pass]
  (go (let [[ok result] (<! (POST "/login"
                                  :username name
                                  :password pass
                                  :route (history/get-state)))]
        (if (and ok result)
          ;; update the entire app state because we can't know what
          ;; data was just pulled down.
          ;; Note that we need to include the route because otherwise
          ;; it gets wiped, and we have to re-query for it because
          ;; login is async the route may have changed.
          (app-state/put-update! [] (merge result {:route (history/get-state)}))
          (log "some problem" ok result)))))

(defn signup
  "Signup with a username and a password + confirmation of password."
  [name pass confirm]
  (go (let [[ok result] (<! (POST "/signup" :username name
                                  :password pass :confirm confirm
                                  :route (history/get-state)))]
        (if (and ok result)
          (app-state/put-update! [] (merge result {:route (history/get-state)}))
          (log "some problem: " ok result)))))

(defn logout
  "Logout. Once logged out, re-route to /"
  []
  (go (let [[ok result] (<! (POST "/logout"))]       
        (when ok
          (dorun (map
                  (fn [[key val]] (app-state/put-update! [key] val))
                  (merge result {:user nil})))
          (app-state/put-update! [:route] "/")
          (history/push-state! "/")))))
