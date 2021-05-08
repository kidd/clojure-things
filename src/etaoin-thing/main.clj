(ns etaoin-thing.main
  (:require [clojure.string :as str]
            [etaoin.keys :as k])
  (:use etaoin.api))

(def driver (chrome {:headless true}))

(defn- price-of-element [el]
  (-> (get-element-text driver el)
       (str/replace  #"^EUR " "")
       (str/replace  #"," ".")
       Float/parseFloat))

(defn get-book [title author]
  (go driver "https://www.iberlibro.com")
  (wait-visible driver [{:id :hp-search-author}])

  (fill driver {:tag :input :id :hp-search-author} author)
  (fill driver {:tag :input :id :hp-search-title} title k/enter)

  (wait-visible driver [{:id :srp-item-price-1}])

  (+ (price-of-element {:id :srp-item-price-1})
     (price-of-element {:id :srp-item-shipping-price-1})))

;; (println (get-book "The art of decision making" "helga drummond"))

;; (quit driver)
