(ns ^{:doc "Entry point for the server side portion of the application.
            Provides functions meeting the requirements of Stuart Sierra's
            workflow (http://thinkrelevance.com/blog/2013/06/04/clojure-workflow-reloaded)
            It is rare to directly call anything in here. When working at the
            repl, you'll use (reset), which calls (stop) and (start)"}
    birds.system 
  (:require [birds.server :as server]
            [environ.core :refer [env]]
            [birds.web-page :as web-page]))

(defn create-system
  "Returns a new instance of the whole server-side application"
  []  
    {:port (Integer/parseInt (or (env :port) "8000"))
     :handler (web-page/create-handler)})

(defn start
  "Performs side effects to initialize the system, acquire resources,
  and start it running. Returns an updated instance of the system."
  [system]
  (let [server (server/create (:handler system)
                              :port (:port system))]
    (into system
          {:server server})))

(defn stop
  "Performs side effects to shut down the system and release its
  resources. Returns an updated instance of the system."
  [system]
  (when (:server system)
    (server/stop (:server system)))
  (dissoc system :server))
