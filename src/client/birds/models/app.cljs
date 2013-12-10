(ns birds.models.app
  "The internal representation of the state of the application, and functions + queues for interacting with it."
  (:require [birds.queue :refer [observable-queue] :as oqueue]
            [cljs.core.async :refer [chan >! <!] :as async]
            [birds.util :refer [log POST]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def app-state 
  "Internal representation of the state of the application
  todo: privatize"
  (atom {}))

(def ^:private input-chan 
  "Channel for giving input to the application (to request transforms or effects or both) from a user.
  Accepts messages that look like [[:path :to :relevant :part :of :app-state] :operation-to-perform :any 'args']"
  (chan 10))

(def ^:private effect-chan
  "Channel for handling input that requires communication w/ server
  Accepts messages that look like: [[:path :to :target] :operation old-app-state parameters-map]"
  (chan 10))

(def ^:private transform-chan
  "Channel for handling transforming the application state.
  Accepts messages that look like: [[:path :to :target] :operation old-app-state parameters-map]"
  (chan 10))

(def ^:private update-chan
  "Channel for handling direct updates to application state, 
  generally using data sent from the server.
  Accepts messages that look like [:target new-value]"
  (chan 10))

(def ^:private output-queue
  "Queue to which application elements (probably views) may subscribe for updates.
  Messages will appear in the format: [target old-state-at-target new-state-at-target old-state-of-app new-state-of-app]"
  (observable-queue (chan)))

(defn sub
  "Subscribe to updates from the output queue, optionally supplying a predicate,
  to only receive updates for which the predicate is truthy.
  Returns the channel that will receive the messages, so you may either pass in a channel
  or not."
  ([] (sub (chan)))
  ([ch] (sub ch (constantly true)))
  ([ch pred] (oqueue/subscribe output-queue ch pred)))

(defn unsub
  "Unsubscribe a channel from updates on the output queue.
  Useful (and probably too easy to forget) when a view is being destroyed."
  [ch] (oqueue/unsubscribe output-queue ch))

(defn put-input!
  "Put a message onto the input channel"
  [& msg]
  (async/put! input-chan msg))

(defn put-update!
  "Put a message on the update channel"
  [& msg]
  (async/put! update-chan msg))

(defn paths-matching 
  "A commonly used predicate for subscribing to the output queue.
  Returns a predicate that is truthy when the path of the message matches any
  of 'paths'"
  [paths]
  (fn [[path]] (some (partial = path) paths)))


(defn target-matches?
  "Determines whether the input-target is a match for a transform target.
  Input targets are literal paths to values on the app model,
  while transform targets are paths which may contain wildcards.
  [:route] = [:*]
  [:current-user] = [:current-user]
  [:groups :'group-id' :name] = [:groups :* :name]
  [:songs :'song-id' :name] = [:songs :**]
  Todo: there must be a saner way to express this."
  [input-target transform-target]
  (let [empty-in (empty? input-target)
        empty-tran (empty? transform-target)]
    (if (and empty-in empty-tran)
      true
      (if (or empty-in empty-tran)
        (if empty-in
          (= transform-target [:**])
          false)
        (let [in (first input-target)
              tran (first transform-target)]
          (if (= tran :**)
            true
            (if (= tran :*)
              (target-matches? (rest input-target) (rest transform-target))
              (and (= in tran)
                   (target-matches?
                    (rest input-target)
                    (rest transform-target))))))))))


(defn op-matches?
  "Determines whether the input-op matches the transform op.
	Input op is a keyword with the literal name of an operation,
	while transform op may contain a wildcard."
  [in trans]
  (or (= trans :*) (= trans in)))

(defn match-target-and-op
  "Given a seq of [target op fn] tuples,
  return the first one matching target and op"
  [fns target op]
  (nth 
   (first 
    (filter 
     (fn [[f-target f-op]] 
       (and (target-matches? target f-target) 
            (op-matches? op f-op))) 
     fns)) 
   2))

;;; ====================================================
;;; TRANSFORMS

(def transforms
  "Transformations that may be performed on the application state."
  (atom []))

(defn register-transform! [target op f]
  (swap! transforms conj [target op f]))

(defn identity-transform
  "Set the value of the target path to (:val params)"
  [target op old-val app-state params]
  (:val params))

(defn merge-transform
  [_ _ old-val _ params]
  (merge old-val params))

;; any updates that only need to set some location in the app-state to a val
;; may call (put-update! [:target] new-val)
;;       or (put-input! [:target] :set :val "new val")
;; I haven't figured out yet if one is preferable.
(register-transform! [:**] :set identity-transform)

(register-transform! [] :merge merge-transform)


;;; =====================================================
;;; EFFECTS

(def effects
  "Effects that the application my have on the outside world,
  generally by talking to a server."
  (atom []))

(defn register-effect!
  "Add an effect function"
  [target op f]
  (swap! effects conj [target op f]))

(defn call-effect-api
  "Given a location and variadic params of the format :name val,
  POST to api/location with those params"
  [loc & params]
  (POST (str "/" (name loc)) (apply hash-map params)))

(defn default-effect
  "Wrap a fn to call call-effects-api.
  Allows effect fns to be pure fns that just return the location to
  call and params to use."
  [fun]
  (fn [& args]
    (let [[loc & kvs] (apply fun args)]
      (if (even? (count kvs))
        (apply call-effect-api loc kvs)
        (apply call-effect-api loc (concat (butlast kvs) (flatten (vec (last kvs)))))))))

;; example usage of effect fns:
#_(defn my-effect-fn [target op app-state params]
    [:set-group-name :id (:id (:viewed-group app-state)) :name (:val params)])
#_(register-effect! [:viewed-group :name] :set (default-effect my-effect-fn))



;;; initializations

(defn init-input-chan
  "Start reading from the input chan and writing messages to the
   effect and transform chans"
  []
  (go (while true
        (let [[target op & rest :as msg] (<! input-chan)
              params (apply hash-map rest)
              old-state @app-state]
          (>! effect-chan [target op old-state params])
          (>! transform-chan [target op old-state params])))))

(defn init-effect-chan
  "Start reading from the effect chan and
   calling effect fns with msgs"
  []
  (go (while true
        (let [[target op old-state params] (<! effect-chan)
              effect-fn (match-target-and-op @effects target op)]
          (when effect-fn
            (effect-fn target op old-state params))))))

(defn init-transform-chan
  "Start reading from transform chan and transforming app
  state + writing to output queue when msgs are received"
  [] 
  (go (while true
        (let [[target op old-state params] (<! transform-chan)
              trans-fn (match-target-and-op @transforms target op)]          
          (when trans-fn
            (let [state-at-target (get-in old-state target)
                  result (trans-fn target op state-at-target old-state params)]
              (when-not (= state-at-target result)              
                ;; i don't love this:
                ;; have to special case when the target is []
                ;;  because update-in expects at least one key to target...
                (let [new-state
                      (swap! app-state
                             (fn [state] (if (= target [])
                                           (merge state result)
                                           (update-in state target (constantly result)))))]
                  (oqueue/put! output-queue
                               [target state-at-target result old-state new-state])))))))))

(defn init-update-chan
  "Start reading from update chan and writing to transform chan when msgs come in." 
  []
  (go (while true
        (let [[target new] (<! update-chan)]
          (log "update chan say" target new)
          (>! transform-chan [target :set @app-state {:val new}])))))

(defn init []
  (init-input-chan)
  (init-effect-chan)
  (init-transform-chan)
  (init-update-chan))
