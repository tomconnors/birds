(ns birds.routes
  (:require [birds.util :refer [log]]
            [clojure.string :as string]
            [cljs.core.async :refer [chan >! <! put! alts! close!]]
            [birds.models.app :as app-state]
            [birds.components.home :refer [Home]]
            [birds.components.team :refer [Team]])
  (:require-macros [cljs.core.async.macros :refer [go alt!]]))


(def routes 
  "The routes for the application. Todo: should be handled externally to this file. 
  This file should be removed and replaced with secretary"
  {"/" Home
   "/team/:id" Team})

(def ^:private slash #"/")

(defn- param? [r]
  (= (first r) \:))

(defn- component-matches? [r u]
  (or (param? r) (= r u)))

(defn- extract-component [r u]
  (when (param? r) 
    {(keyword (subs r 1)) u}))

(defn- exact-match? [r u]
  (= r u))

(defn route-matches?
  "A predicate to determine if a route matches a URI path."
  [route uri-path]
  (let [r (string/split route slash)
        u (string/split uri-path slash)]
    (when (= (count r) (count u))
      (every? true? (map #(component-matches? %1 %2) r u)))))

(defn any-matches?
  "Determines if there are any routes that match a given URI path."
  [uri-path]
  (some #(route-matches? (first %) uri-path) routes))

(defn extract-components
  "Extract the match data from the URI path into a hash map"
  [route uri-path]
  (when (route-matches? route uri-path)
    (apply merge
           (for [z (zipmap (string/split route slash) (string/split uri-path slash))
                 :let [c (apply extract-component z)]
                 :when (not (nil? c))]
             c))))

(defn filter-routes [f uri-path]
  (filter #(f (first %) uri-path) routes))

(defn route [uri-path]
  "figures out which route is represented by the uri-path
  and calls that route with any params in the uri-path"
  (if-let [[route action] (first (filter-routes exact-match? uri-path))]
    [action {}]
    (when-first [[route action] (filter-routes route-matches? uri-path)]
      [action (extract-components route uri-path)])))
