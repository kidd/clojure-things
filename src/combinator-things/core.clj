(ns combinator-things.core)

(defn apply-if
  "if(p(m)) then f(m, ...args) else m

  Useful for `maybe-*` like updates."
  [m p f & args]
  (if (p m)
    (apply f m args)
    m))

(cond-> {:a 1}
  true (update :a inc )
  odd?)

(-> {:a ["a b a"]}
    (update :a apply-if string? str/replace #"a" "b")
    (update :a apply-if vector? update 0 str/replace #"a" "b"))

(-> {:a "a b a"}
    (update :a apply-if string? str/replace #"a" "b")
    (update :a apply-if vector? update 0 str/replace #"a" "b"))

(-> {:a "a b a"}
    (update :a apply-if string? vector)
    (update :a apply-if vector? update 0 str/replace #"a" "b"))

(-> {:a "a b a"}
    (update :a apply-if string? vector)
    (update :a update 0 str/replace #"a" "b"))

(-> {:a "a b a"}
    (update :a apply-if string? vector)
    (update-in [:a 0] str/replace #"a" "b"))

(with-redefs [jdbc/execute!
              (fn [& args]
                (apply old-jdbc-exec!
                       (cond-> (vec args)
                         (and (= :development (config/app-env)) (string? (get (vec args) 1)))
                         (update 1 str/replace #"(?i)CREATE TABLE" "create unlogged table")
                         (and (= :development (config/app-env)) (vector? (get (vec args) 1)))
                         (update-in [1 0] str/replace #"(?i)CREATE TABLE" "create unlogged table"))))])

                       (cond-> (vec args)
                         (and (= :development (config/app-env)) (string? (get (vec args) 1)))
                         (update 1 apply-if string? vector)
                         (and (= :development (config/app-env)))
                         (update-in [1 0] str/replace #"(?i)CREATE TABLE" "create unlogged table"))


(-> (vec args)
    (update 1 apply-if string? vector)
    (update-in [1 0] str/replace #"(?i)CREATE TABLE" "create unlogged table"))

(defn fork [l c r]
  #(->> % ((juxt l r)) (apply c)))

(defn avg [a]
  ((fork (partial apply +) / count) a))

(defn unique? [a]
  ((fork count = (comp count set)) a))


(defn over [g h]
  (fn
    ([w]   (g (h w)))
    ([a w] (g (h a) (h w)))))

(deftest subscription-version-schema-test
  (let [[version-id version-created-at & original-fields]
        (db/describe "subscription_version")
        cleanup
        (fn [x] (map #(dissoc % :is_nullable :column_default) x))]
    (is ((over = cleanup) original-fields (db/describe "subscription")))))
