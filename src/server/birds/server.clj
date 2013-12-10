(ns birds.server
  (:require [ring.adapter.jetty :as jetty]))

(defn create
  "Create and return a server instance, listening on the supplied port."
  [handler & {:keys [port]}]
  {:pre [(not (nil? port))]}
  (let [server (jetty/run-jetty handler {:port port :join? false})]
    (println (str "Started server on port " port))
    server))

(defn stop
  "Cause the server to stop listening"
  [server] (.stop server))
