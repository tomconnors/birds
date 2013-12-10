(ns birds.web-page
  (:require [compojure.core :refer [GET POST ANY routes]]
            [compojure.route :refer [not-found resources]]
            [compojure.handler]           
            [ring.util.response :refer [response] :as resp]
            [ring.middleware.edn :refer [wrap-edn-params]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.session.cookie :refer [cookie-store]]            
            [clojure.string :as string]
            [ring.util.response :as resp]
            [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])
            [birds.routes.home :as home-route]
            [birds.routes.team :as team-route]
            [birds.routes.user :as user-route]
            [birds.routes.auth :as auth-routes]
            [birds.routes.util :as route-util :refer [route-handler]]
            [birds.models.users :as users]
            [birds.util :refer [log]]))

(defn with-uri-rewrite
  "Rewrites a request uri with the result of calling f with the
   request's original uri.  If f returns nil the handler is not called."
  [handler f]
  (fn [request]
    (let [uri (:uri request)
          rewrite (f uri)]
      (if rewrite
        (handler (assoc request :uri rewrite))
        nil))))

(defn- uri-snip-slash
  "Removes a trailing slash from all uris except /"
  [uri]
  (if (and (not (= "/" uri))
           (.endsWith uri "/"))
    (.substring uri 0 (dec (count uri)))
    uri))

(defn- single-slashes
  "Replace all occurrences of multiple slashes with a single slash"
  [uri]
  (string/replace uri #"/+" "/"))

(defn ignore-trailing-slash
  "Makes routes match regardless of whether or not a uri ends in a slash."
  [handler]
  (with-uri-rewrite handler uri-snip-slash))

(defn replace-duplicate-slashes
  "Wrap a handler to replace duplicate slashes in requested routes"
  [handler]
  (with-uri-rewrite handler single-slashes))

(defn create-handler*
  "return a handler fn that responds to each of the listed routes with
   the supplied handler"
  []
    (apply routes
           (concat (team-route/routes route-handler)
                   (auth-routes/routes route-handler)
                   (home-route/routes route-handler)
                   (user-route/routes route-handler)
                   [(resources "/")])))

(defn reify-edn
  "Ring Middleware.
   When edn is provided under the :edn-data key of the params of a
   request, assoc the reified value of that edn into the params.
   Not sure yet whether there is a better way to do this -
   I've only included this because there's no way to supply edn in a get
   request, though that limitation might not be a real problem,
   as get reqs generally don't supply complex data structures."
  [handler]
  (fn [req]        
    (if-let [data (:edn-data (:params req))]
      (handler (assoc req :params (read-string data)))
      (handler req))))


(defn create-handler
  "Create a handler fn to handle server requests."
  []
  (-> (create-handler*)
      (ignore-trailing-slash)
      (replace-duplicate-slashes)
      (reify-edn)
      (wrap-edn-params)
      (compojure.handler/site {:session
                               {:store
                                (cookie-store
                                 ;; todo: a secret, secure key.
                                 {:key "abcdwertyuioplkj"})}})
      (friend/authenticate {:allow-anon? true
                            :login-uri "/login-friend"
                            :default-landing-uri "/"
                            :workflows [(workflows/interactive-form)]
                            :credential-fn
                            (partial creds/bcrypt-credential-fn
                                     users/find-user-by-name)})))
