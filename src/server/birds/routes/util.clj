(ns birds.routes.util
  (:require [cemerick.austin.repls :refer [browser-connected-repl-js]]
            [cemerick.friend :as friend]
            [birds.templates :refer [layout]]))

(defn is-xhr
  "Determines whether the request context is xhr, using the stupidest
  means imaginable. Do something smarter, oh glorious future Tom!"
  [context]
  (not (re-find #"html" ((:headers context) "accept"))))

(defn page-data
  "Supply the data that should be included with a page at the first page load.
  Currently, this is just the user, as the only time a user needs to be re-supplied
  is when they log out or in, not on every xhr request."
  [context user]
  (if-let [user (friend/current-authentication (friend/identity context))]
    {:user (select-keys user [:id :name :img :email])}
    {:user nil}))

(defn req->user [req]
  (friend/current-authentication (friend/identity req)))

(defn route-handler
  "Given a function to get the data for a route,
   return a ring handler that calls the data fn with the req and current user,
   and responds with either just the data (xhr) or a full page (normal GETs)"
  [data-fn]
  (fn [req]
    (let [user (req->user req)]
      (let [data (data-fn req user)]
        (if (is-xhr req)
          ;; xhr requests just want edn data
          (str data)
          ;; otherwise return an html doc
          (layout (merge data (page-data req user))))))))

(defn with-request-params
  "Read each of 'param-names' from the request and call f with the values,
  in the order they are provided in the call to this fn.
  Also always provides the current user for free."
  [f & param-names]
  (fn [{req-params :params :as req}]
    (apply f (concat (map #(% req-params) param-names) [(req->user req)]))))
