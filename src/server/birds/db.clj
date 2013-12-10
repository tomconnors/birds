(ns birds.db 
  "an in-memory database. In a real application you'd probably want something like 
   postgres or datomic, depending upon how much koolaid you drink"
  )

(def initial-val {:user []
                  :team []
                  :team-member []
                  :sighting []})

(def db (atom initial-val))

;; example user:
#_{:name "bill"
   :password "some string"
   :id 'a-number}

;;example team:
#_{:name "The bird mans"}

;; example sighting
#_{:user-id 'some-user-id
   :bird-type "canary"
   :count 3}

;; example team-member
#_{:team-id 'a-number
   :user-id 'a-number
   :access-level (or :member :owner)}

(defn insert! [table doc]
  (swap! db (fn [old-val] (assoc old-val table (conj (table old-val) doc)))))

(defn read [table doc]
  (let [d @db]
    (filter #(= % (merge % doc)) (table d))))

(defn read-one [table doc]
  (first (read table doc)))

(defn delete! [table doc]
  (swap! db (fn [old-val] (assoc old-val table
                                 (remove #(= (merge % doc) %) (table old-val))))))

(defn clear-all! []
  (reset! db initial-val))
