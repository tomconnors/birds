(ns birds.components.app
  (:require [pump :as pump]
            [react :as react]
            [birds.components.nav :refer [Nav toggle-main-nav show-nav-with-login]]
            [birds.components.messenger :refer [Messenger]]
            [birds.util :refer [log GET]]
            [birds.routes :as routes]
            [birds.history :as history]
            [birds.models.app :as state]
            [dommy.core :refer [listen! attr]]
            [cljs.core.async :refer [chan <! >!]])
  (:require-macros [pump.macros :refer [defr] :as pump-macros]
                   [cljs.core.async.macros :refer [go]]))


(def body (.-body js/document))

(def content-el "the element into which the application will be rendered"
  (.querySelector js/document "#root-mount-point"))

(defr Header
  [c {user :user} _]
  [:header
   ;; because this works, apparently: ☰
   [:a.nav-toggle {:title "Menu" :on-click #(toggle-main-nav %)} "☰"]
   [:a.main-title {:href "/"} "birds"]
   [:div#header-right
    (if user
      [:a#main-user-btn.btn.btn-default {:href (str "/user/" (:id user))}
           [:img.user-btn__avatar {:src (:img user)}]
           ;; todo: this should be wrapped in a conditional and only shown
           ;; when not on mobile.
           [:span (:name user)]]
      [:button#main-user-btn.btn.btn-default.login-btn
       {:on-click show-nav-with-login}
       "Log In"])]])

(defr Content [_ p _]
  (let [[route-fn route-args] (routes/route (:route p))]    
    [route-fn (merge p {:route-args route-args})]))

(defn handle-nav
  "Handle navigation-causing events
  (clicking an anchor or hitting the back/forward buttons)."
  [location]
  (go (let [[ok result] (<! (GET location))]
        (if ok 
          (do
            (dorun (map (fn [[key val]] (state/put-update! [key] val)) result))
            (state/put-update! [:route] location))
          (state/put-update! [:error]
                             (str "Failed to fetch data for " location))))))


(defn handle-anchor-click [anchor]
  (let [location (attr anchor :href)]
    (if-not (= location (history/get-state))
      (history/push-state! location))))

(defn handle-body-clicks [e]
  (loop [target (.-target e)]
    (if (and target (not= "a" (.. target -tagName toLowerCase)))
      (recur (.-parentElement target))
      (when target
        (.preventDefault e)
        (handle-anchor-click target)))))

(defr Layout
  [component prop _]
  [:div
   [:div.nav-slide-wrap {:on-click handle-body-clicks}
    [Nav prop]
    [:div {:class-name (str "main-body " (if (:user prop) "signedIn" "notSignedIn"))}
     [Header prop]
     [Content prop]]]
   [Messenger {}]])

(defn init []
  (history/on-state-change handle-nav)
  (go (let [ch (chan)
            [_ _ _ _ new-state] (<! (state/sub ch))
            layout (react/render-component (Layout new-state) content-el)]
        (loop []
          (let [[_ _ _ _ new] (<! ch)]   
            (.replaceProps layout new)
            (recur))))))



