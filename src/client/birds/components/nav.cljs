(ns birds.components.nav
  (:require [dommy.core :refer
             [toggle-class! remove-class! add-class! listen! unlisten!
              has-class? set-html! append!] :as dommy]
            [pump :as pump]
            [react :as react]
            [birds.dom :as dom :refer [kill-event]]
            [birds.auth :as auth]
            [birds.util :refer [log POST GET]]
            [birds.shared.models.users :refer [valid-username? valid-password?]]
            [cljs.core.async :refer [>! <!]]
            [birds.models.app :as app-state])
  (:require-macros [dommy.macros :refer [sel sel1]]
                   [pump.macros :refer [defr]]
                   [cljs.core.async.macros :refer [go]]))


(defn focus [node]
  (.focus node)
  node)

(def main-nav-toggle-class "main-nav-visible")

(defn nav-is-open? []
  (has-class? dom/body main-nav-toggle-class))

(declare on-window-click)

(defn hide-main-nav []
  (remove-class! dom/body main-nav-toggle-class)
  (unlisten! dom/body :click on-window-click))

(defn show-main-nav []
  (add-class! dom/body main-nav-toggle-class)
  (listen! dom/body :click on-window-click))

(defn on-window-click [e]
  (if-not (.contains (.querySelector js/document "#main-nav-container") (.-target e))
    (hide-main-nav)))

(defn toggle-main-nav [e]
  (.stopPropagation e)
  (if (nav-is-open?) (hide-main-nav) (show-main-nav)))

(defn create-main-nav [el app-state]
  (append! el (templates/main-nav app-state)))
 
(defn switch-form [e show hide]
  (kill-event e)
  (let [show (sel1 show)
        hide (sel1 hide)]
    (dommy/hide! hide)
    ;; using display block rather than show! because el has a
    ;; display:none property and show! doesn't override it.
    (-> show (dommy/set-style! :display "block") (focus))))

(defn show-login [e]
  (switch-form e :#nav-login :#nav-signup))

(defn show-signup [e]
  (switch-form e :#nav-signup :#nav-login))

(defn show-login-error [str]
  (dommy/set-text! (sel1 :.login-error.auth-error) str))

(defn clear-error [sel]
  (dom/clear! (sel1 sel)))

(def clear-login-error (partial clear-error :.login-error.auth-error))
(def clear-signup-error (partial clear-error :.signup-error.auth-error))

(defn handle-login [e]
  (.preventDefault e)
  (let [username (str (.-value (sel1 "[name=username-login]")))
        password (str (.-value (sel1 "[name=password]")))]
    (if-not (valid-username? username)
      (show-login-error "Invalid Username.")
      (if-not (valid-password? password)
        (show-login-error "Invalid Password.")
        (do
          (clear-login-error)
          (auth/login username password))))))

(defn show-signup-error [str]
  (dommy/set-text! (sel1 :.signup-error.auth-error) str))

(defn handle-signup [e]
  (.preventDefault e)
  (let [username (str (.-value (sel1 "[name=username]")))
        pass1 (str (.-value (sel1 "[name=password1]")))
        pass2 (str (.-value (sel1 "[name=password2")))]
    (if-not (valid-username? username)
      (show-signup-error "Invalid Username")
      (if-not (= pass1 pass2)
        (show-signup-error "Passwords don't match.")
        (if-not (valid-password? pass1)
          (show-signup-error "Invalid Password. Must be > 5 characters.")
          (do
            (clear-signup-error)
            (auth/signup username pass1 pass2)))))))

(defn handle-logout [e]
  (kill-event e)
  (auth/logout))


(defn show-nav-with-login [e]
  (show-main-nav))


(defr NavLoginForm
  [_ _ _]
  [:form#nav-login.auth-form {:name "login" :on-submit handle-login}
   [:input {:type "text" :placeholder "Username" :name "username-login"}]
   [:input {:type "password" :placeholder "Password" :name "password"}]
   [:span.login-error.auth-error]
   [:input.btn.btn-default {:type "submit" :name "submit" :value "Do It"}]
   [:input.btn.btn-default {:type "button" :name "recover" :value "Forgot Password?"}]])

(defr NavSignupForm
  [_ _ _]
  [:form#nav-signup.auth-form {:name "signup" :on-submit handle-signup}
   [:input {:type "text" :placeholder "Username" :name "username"}]
   [:input {:type "password" :placeholder "Password" :name "password1"}]
   [:input {:type "password" :placeholder "Password Again" :name "password2"}]
   [:span.signup-error.auth-error]
   [:input.btn.btn-default {:type "submit" :name "submit" :value "Do It"}]])

(defr NavLogin
  [c _ s]
  [:li.nav-auth
   [:ul.nav.nav-tabs
    [:li.active {:name "login"}
     [:a.nav-auth-button {:on-click show-login} "Log In"]]
    [:li {:name "signup"}
     [:a.nav-auth-button {:on-click show-signup} "Sign Up"]]]
   [NavLoginForm]
   [NavSignupForm]])
 
(defr Nav
  [_ {user :user :as params} _]
  [:div#main-nav-container {:key "main-nav-container"}
   [:ul.main-nav
    (if-not user [NavLogin])
    [:li [:a.nav-item {:href "/"} "Home"]]    
    (when user
      [:li
       [:a.nav-item {:href "/" :name "logout" :on-click handle-logout}
        (str "Log Out " (:name user))]])]])
