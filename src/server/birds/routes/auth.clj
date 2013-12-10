(ns birds.routes.auth
  "Handlers for routes dealing with authentication - signup,
  login, and logout.
  Todo: this namespace duplicates some routing logic to allow picking up
  updated data when a user signs in or out. How to DRY?"
  (:require [cemerick.friend :as friend]
            [ring.util.response :refer [response redirect redirect-after-post]]
            [cemerick.friend
             [workflows :as workflows]
             [credentials :as creds]]
            [birds.models.users :as users]
            [clout.core :refer [route-matches]]
            [birds.routes.util :refer [page-data]]
            [birds.routes.home :as home]
            [birds.util :refer [log]]
            [compojure.core :refer [GET POST ANY]]
            [birds.shared.models.users
             :refer [valid-username? valid-password?]]))

(defn route-data
  "Return the data for a route, based on the :uri of the req.
   Uses a verbose version of what compojure does (except it's all
   on the server, no redirects or other such foolishness) -
   ideally the compojure routes defined in com.web-page could be
   reused, but I don't know how yet."
  [req user] 
  (or
   (if-let [params (route-matches "/" req)]
     (home/data user))))

(defn resp-with-user
  "Respond with the data for the callback route of the request"
  [req user]
  (response (str (merge
                  {:user (if user (select-keys user [:name :id]) nil)}
                  (route-data (assoc req :uri (:route (:params req))) user)))))
 
(defn merge-auth
  "Get the new data for the user who just logged in or signup, make a response,
   merge friend authentication into that response, and return the response."
  [req user]
  (let [auth-user (select-keys user [:name :id])]    
    (friend/merge-authentication
     (-> (resp-with-user req auth-user) 
         (assoc :session (:session req)))
     (assoc auth-user :identity (:id user)))))

(defn merge-no-auth
  "Fetch the data for a user who just logged out"
  [req]
  (-> (response
       (str (merge {:user nil}
                   (route-data (assoc req :uri "/") nil))))
      (assoc :session (:session req))))

(defn signup-handler
  "Handle a user attempting to sign up."
  [{{:keys [username password confirm] :as params} :params :as req}]
  (if (valid-username? username)
    (if (users/free-username? username)
      (if (= password confirm)
        (if (valid-password? password)
          (let [user (users/insert-user!
                      {:name username
                       :password (creds/hash-bcrypt password)})]            
            (merge-auth req user)))))))

(defn logout-handler
  "Handle a user attempting to log out."
  [req]
  (friend/logout* (merge-no-auth req)))

(defn login-handler
  "Handle a user attempting to sign in."
  [{{:keys [username password] :as params} :params :as req}]
  (let [user (creds/bcrypt-credential-fn users/find-user-by-name params)]
    (if user
      (merge-auth req user)
      (merge-no-auth req))))


(defn routes [route-handler]
  [(POST "/signup" req signup-handler)
   (POST "/login" req login-handler)
   (ANY "/logout" req logout-handler)])
