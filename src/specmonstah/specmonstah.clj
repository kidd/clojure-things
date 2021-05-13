(ns specmonstah.short-sweet
  (:require [reifyhealth.specmonstah.core :as sm]
            [reifyhealth.specmonstah.spec-gen :as sg]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]))




(def id-seq (atom 0))
(s/def ::id (s/with-gen pos-int? #(gen/fmap (fn [_] (swap! id-seq inc))
                                            (gen/return nil))))
(s/def ::not-empty-string (s/and string?
                                 not-empty
                                 #(< (count %) 10)))

(s/def ::username ::not-empty-string)
(s/def ::user (s/keys :req-un [::id ::username]))

(s/def ::created-by-id ::id)
(s/def ::content ::not-empty-string)
(s/def ::post (s/keys :req-un [::id ::created-by-id ::content]))

(s/def ::post-id ::id)
(s/def ::like (s/keys :req-un [::id ::post-id ::created-by-id]))

(def schema
  {:user {:prefix :u
          :spec ::user}
   :post {:prefix :p
          :spec ::post
          :relations {:created-by-id [:user :id]}}
   :like {:prefix :l
          :spec ::like
          :relations {:post-id [:post :id]
                      :created-by-id [:user :id]}
          :constraints {:created-by-id #{:uniq}}
          }})


(sm/add-ents {:schema schema} {:user [[3]]})
