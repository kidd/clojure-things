(ns apl-power-op.core)

;; https://xpqz.github.io/learnapl/iteration.html
(defn good-enuf [x y]
  (< (abs (- x y)) 0.0001))

(defn power [pred f]
  #(take-while (complement pred)
               (partition 2 (iterate f %))))

(defn power [pred f]
  #(last
    (last
     (take-while (complement pred)
                 (partition 2 (iterate f %))))))

(last
 (last
  ((power #(good-enuf 0 (apply - %)) #(/ % 2)) 10)))

(take 3 (iterate last ((power #(good-enuf 0 (apply - %)) #(/ % 2)) 10))) ; shows the path navigating the different structure depth
(nth (iterate last ((power #(good-enuf 0 (apply - %)) #(/ % 2)) 10)) 2) ; takes the nth

;; we can use power again
