(ns user
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.pprint :refer (pprint)]
            [clojure.repl :refer :all]
            [clojure.test :as test]
            [clojure.tools.namespace.repl :refer (refresh refresh-all)]
            [birds.system :as system]))

(defonce the-system
  ;; "A container for the current instance of the application.
  ;; Only used for interactive development."
  ;;
  ;; Don't want to lose this value if this file is recompiled (when
  ;; changes are made to the useful additional utilities for the REPL
  ;; at the end of the file), so use `defonce`.
  ;; But note that this /is/ blatted when a `reset` is done.
  nil)

(defn create
  "Creates a system and makes it the current development system."
  []
  (alter-var-root #'the-system
    (constantly (system/create-system))))

(defn start
  "Starts the current development system."
  []
  (alter-var-root #'the-system system/start))

(defn stop
  "Shuts down and destroys the current development system."
  []
  (alter-var-root #'the-system
    (fn [s] (when s (system/stop s)))))

(defn create-and-start
  "Creates a system, makes it the current development system and starts it."
  []
  (create)
  (start))

(defn reset []
  "Stop, refresh and create-and-start."
  (stop)
  (refresh :after 'user/create-and-start))

(defn cljs-repl
  "Fire up a browser-connected ClojureScript REPL"
  []
  (let [repl-env (reset! cemerick.austin.repls/browser-repl-env
                         (cemerick.austin/repl-env))]
    (cemerick.austin.repls/cljs-repl repl-env)))

