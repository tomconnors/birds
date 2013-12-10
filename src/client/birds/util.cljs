(ns birds.util
  (:require [cljs.core.async :as async :refer
             [chan <! >! timeout sliding-buffer put! take!]]
            [ajax.core :as ajax]
            [goog.date.relative :as date])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(defn log 
  "Variadic delegation to js/console.log.
  Returns the first arg."
  [& args]
  (.apply (.-log js/console) js/console (clj->js args))
  (first args))

(defn str-contains? 
  "Does s contain check?"
  [s check]
  (not (= -1 (.indexOf s check))))

(defn put-all!
  "Write x to all the chans. Joyfully ripped off from David Nolen."
  [cs x]
  (doseq [c cs] (go (>! c x))))

(defn- ajax-req
  ([type url] (ajax-req type url {}))
  ([type url first-arg-key first-arg-val & rest]
     (ajax-req type url (apply hash-map first-arg-key first-arg-val rest)))
  ([type url args] (let [ch (chan)
                         param {:params (if (= type :get)
                                          {:edn-data  (pr-str args)}
                                          args)
                                :handler  #(put! ch %)
                                :format (ajax/edn-format)}]
                     (ajax/ajax-request url type param)
                     ;; return the chan
                     ch)))

(def GET (partial ajax-req :get))
(def POST (partial ajax-req :post))

(defn defer [fun]
  (.setTimeout js/window fun 1))

(defn toggle-bool
  "useful for toggling boolean values in an atomic swap!
  Call like : (swap! c toggle-bool :show)"
  [val prop]
  {prop (not (prop val))})

(defn pluralize
  "pluralize `singular` if `num` isn't 1.
   Provide `plural` if the plural form isn't just `singular` + 's'"
  ([num singular] (pluralize num singular nil))
  ([num singular plural]
     (if (= num 1)
       singular
       (if plural
         plural
         (str singular "s")))))


(defn pretty-date
  "Google Closure's chance to shine. Given a date, d, return a human-readable
   string, something like '6 days ago'"
  [d]
  (date/format d))
