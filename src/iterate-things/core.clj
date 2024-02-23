(ns iterate-things.core)

(->> [:a]
     (map {:a [:b :d] :b :c})
     flatten
     distinct
     (remove nil?)
     (map {:a [:b :d] :b :c})
     (remove nil?))

(m/take-upto #(some #{:c} %)
             (iterate (comp
                       #(remove nil? %)
                       flatten
                       #(map {:a [:b :d] :b :d :d :c} %))
                      [:a]))
