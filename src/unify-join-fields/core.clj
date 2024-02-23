(ns unify-join-fields.core
  (:require [harbormaster.util :as u]))


;;; problem statement is: I have a list of records I got from a
;;; left-join, meaning I have full rows for what would be a 1 to many
;;; relationship.  I want to "hydrate" the relation into a {,,, :relation [{:id 1} {:id 2}]}


;;; (def hh [{:a 1, :b 2, :c 3} {:a 1, :b 2, :c 4} {:a 99, :b 2, :c 3}])
;;; given there's the main record that has [:a :b] and the joined relation is [:c]
;;; I want [{:a 1  :b 2 :relation [{:c 3} {:c 4}]}
;;;         {:a 99 :b 2 :relation [{:c 3}]} ]


;;; Caveat: If in the original select, we wouldn't add :a and :b that
;;; make the main table differentiable, that's not possible at all

(def hh [{:a 1, :b 2, :c 3 :aa 4} {:a 1, :b 2, :c 4 :aa 4} {:a 99, :b 2, :c 3 :aa 5}])
(def rname :trial-ups)
(def main-f [:b :a])

;; (let [record (-> (group-by (juxt :a :b) hh) vals ffirst )
;;       [split orig] (u/split-map-by-keys [:a])
;;       _ 1])

(->> (group-by  #(select-keys % main-f) hh)
     (m/map-vals (partial map #(apply dissoc % main-f)))
     (map (fn [[k v]] (assoc k rname (vec v)))))

;; (->> (map #(u/split-map-by-keys % main-f) hh)
;;      (group-by first)
;;      (m/map-vals #(map second %))
;;      (map (fn [[k v]] (assoc k rname (vec v)))))

;; first stab, we pivot over the keys of the "root" entity,
;; group by them, and strip the rest out
(defn hydrate-with-main-entity-keys [m ks as]
  (->> (group-by  #(select-keys % main-f) hh)
       (m/map-vals (partial map #(apply dissoc % main-f)))
       (map (fn [[k v]] (assoc k rname (vec v))))))

;; wait,... it would be good to be able to "strip" the child tables instead knowing which is the common part
(defn other-keys [m ks]
  (keys (apply dissoc m ks)))

(defn hydrate-keys [m ks as]
  (->> (group-by  #(select-keys % (other-keys % ks)) m)
       (m/map-vals (partial map #(apply dissoc % (other-keys % ks))))
       (map (fn [[k v]] (assoc k as (vec v))))))

;; select-keys and dissoc are kind of opposite. instead of other-keys, we can just swap them
(defn hydrate-keys [m ks as]
  (->> (group-by  #(apply dissoc % ks) m)
       (m/map-vals (partial map #(select-keys % ks)))
       (map (fn [[k v]] (assoc k as (vec v))))))

(hydrate-keys hh [:aa] :my-as)

(hydrate-keys
 (hydrate-keys hh [:aa] :my-as)
 [:c]
 :my-cs)

(reduce #(apply hydrate-keys %1 %2)
        hh
        [[[:aa] :my-as] [[:c] :my-cs]])




;;;; dealing  with _ is painful

(def res
  (tdb/query {:select    (spread-as [:sub.id :sub.suspend-at :hi.id :hi.custom-domain :trial-up.expires-at :trial-up.status])
              :from      [[Subscription :sub]]
              :left-join [[HostedInstance :hi] [:= :sub.id :hi.subscription-id]
                          [TrialUp :trial-up] [:= :sub.id :trial-up.subscription-id]]})))

(defn spread-as [coll]
  (mapv #(vector % (-> % name (str/replace "." "-") keyword)) coll))

(defn list-prefixed [coll prefix]
  (->> coll first keys
       (filter (comp #(str/starts-with? % (str (str/replace (name prefix) \- \_) "_")) name))
       vec))

(defn renaming-map [cols prefix]
  (into {}
        (map #(vector % (-> % name (str/replace (re-pattern (str "^" (name prefix) "_")) "") keyword)))
        (list-prefixed cols prefix)))

(defn regroup-prefixed [coll prefix]
  (->> (regroup-entity-relation-keys coll (list-prefixed coll prefix) prefix)
       (mapv
        #(m/update-existing % prefix
                            (partial mapv (fn [x] (set/rename-keys x (renaming-map coll prefix))))))))

(reduce regroup-prefixed res [:hi :trial-up])



;;;; with underscores->dashes



  (u/underscores->dashes
   (tdb/query {:select    (into [:sub.id :sub.suspend-at]
                                (spread-as [:hi.id :hi.custom-domain :trial-up.expires-at :trial-up.status]))
               :from      [[Subscription :sub]]
               :left-join [[HostedInstance :hi] [:= :sub.id :hi.subscription-id]
                           [TrialUp :trial-up] [:= :sub.id :trial-up.subscription-id]]}))))

(defn spread-as
  "[:foo.bar :baz.quux] => [[:foo.bar :foo-bar] [:baz.quux :baz-quux]]"
  [coll]
  (mapv #(vector % (-> % name (str/replace "." "-") keyword)) coll))

(defn list-prefixed
  "filter keywords that start with \":prefix-\"."
  [coll prefix]
  (->> coll first keys
       (filter (comp #(str/starts-with? % (str (name prefix) "-")) name))
       vec))

(defn renaming-map [cols prefix]
  "[:blabla :foo-bar :foo-quux], :foo => {:foo-bar :bar :foo-quux :quux}"
  (into {}
        (map #(vector % (-> % name (str/replace (re-pattern (str "^" (name prefix) "-")) "") keyword)))
        (list-prefixed cols prefix)))

(defn regroup-prefixed
  "entrypoint"
  [coll prefix]
  (->> (regroup-entity-relation-keys coll (list-prefixed coll prefix) prefix)
       (mapv
        #(m/update-existing % prefix
                            (partial mapv (fn [x] (set/rename-keys x (renaming-map coll prefix))))))))

(reduce regroup-prefixed res [:hi :trial-up])




invoices[user1]=[i1, i2]

;; u1f1, u1f2,u1f3, i1f1,i1f2...
;; u1f1, u1f2,u1f3, i2f1,i2f2...
