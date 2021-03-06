(ns etaoin-thing.main
  (:require [clojure.string :as str]
            [etaoin.keys :as k])
  (:use etaoin.api))

(def driver (chrome {:headless true}))

(defn- price-of-element [el]
  (-> (get-element-text driver el)
       (str/replace  #"^EUR " "")
       (str/replace  #"," ".")
       (str/replace  #"Gastos de envío" "")
       Float/parseFloat))

(defn get-book [title author]
  (go driver "https://www.iberlibro.com")
  (wait-visible driver [{:id :hp-search-author}])

  (fill driver {:tag :input :id :hp-search-author} author)
  (fill driver {:tag :input :id :hp-search-title} title k/enter)

  (wait-visible driver [{:id :item-shipping-price-1}])

  (+ (price-of-element {:id :item-price-1})
     (price-of-element {:id :item-shipping-price-1})))

;; (println (get-book "The art of decision making" "helga drummond"))

;; (quit driver)

(defn wikipedia-table []
  (go driver "https://wikipedia.org/wiki/Clojure")
  (let [wikitable (query driver {:css "table.infobox.vevent tbody"})
       children-els (children driver wikitable {:css "tr"})]
   (for [row children-els
         :let [text (try (get-element-text-el driver
                                              (child driver row {:css "th"}))
                         (catch Throwable e ""))]
         :when (= "Family" text)]
     (get-element-text-el driver (child driver row {:css "td"})))))
