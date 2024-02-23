(ns defaultdict-things.core
  "Maps with default values, similar to how you can specify default values and blocks to Ruby's Hash.

Examples:

user> (def b (assoc-default {:foo 34 :bar 34} (fn [_ k] (str k))))
#'user/b
user> (get b :foo-bar)
\":foo-bar\"
user> (def m (assoc-default {:foo 34 :bar 34} 343))
#'user/m
user> (get m :not-here)
343
user> (get m :foo)
34
"
  ;; [potemkin "0.3.0-SNAPSHOT"]
  (:require potemkin))

(potemkin/def-map-type MapWithDefault [m map-default]
  (get [_ k default-value]
       (if (contains? m k)
         (get m k)
         (or default-value map-default)))
  (assoc [_ k v]
    (MapWithDefault. (assoc m k v) map-default))
  (dissoc [_ k]
          (MapWithDefault. (dissoc m k) map-default))
  (keys [_]
        (keys m)))

(potemkin/def-map-type MapWithDefaultFn [m map-default-fn]
  (get [_ k default-value]
       (if (contains? m k)
         (get m k)
         (or default-value (map-default-fn m k))))
  (assoc [_ k v]
    (MapWithDefaultFn. (assoc m k v) map-default-fn))
  (dissoc [_ k]
          (MapWithDefaultFn. (dissoc m k) map-default-fn))
  (keys [_]
        (keys m)))

(defn assoc-default [m default]
  (if (fn? default)
    (MapWithDefaultFn. m default)
    (MapWithDefault. m default)))
