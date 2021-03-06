(ns birds.shared.models.users)

(def min-password-length 6)
 
(def username-test #"^[a-zA-Z0-9_\-]{1,20}$")

(defn regex-matches? [re s]
  #+clj (re-matches re s)
  #+cljs (.test re s))

(defn valid-password? [p]
  (>= (count p) min-password-length))

(defn valid-username? [u]
  (regex-matches? username-test u))

