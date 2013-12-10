(ns birds.templates
  (:require [environ.core :refer [env]]
            [cemerick.austin.repls :refer [browser-connected-repl-js]]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [html5 include-css include-js]]))

(defn layout [data]
  (html5
      [:head
       [:title "birds"]
       [:meta {:name "viewport"
               :content
               "width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no"}]
       [:meta {:name "apple-mobile-web-app-capable"
               :content "yes"}]
       [:meta {:name "apple-mobile-web-app-status-bar-style"
               :content "black"}]
       (include-css
        "//netdna.bootstrapcdn.com/bootstrap/3.0.2/css/bootstrap.min.css"
        "/styles/style.css")]
      [:body
       [:div#root-mount-point]
       [:script#init-data {:type "text/edn"} (str data)]
       [:script {:src "/js/lib/react.js"}]
       [:script {:src "/js/lib/history.js"}]
       (if (:dev env)
         (html        
           [:script {:src "/js/gen-dev/out/goog/base.js"}]
           [:script {:src "/js/gen-dev/main.js"}]
           [:script "goog.require('birds.core')"]
           [:script (browser-connected-repl-js)])
         (html
           [:script {:src "/js/gen/main.js"}]))]))