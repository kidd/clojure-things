(ns clj-protocols.core)

;; https://github.com/Stedi/cdk-clj/blob/master/src/stedi/jsii/alpha/impl.clj#L52-L69

(def obj (reify
            clojure.lang.ILookup
           (valAt [this k]
             (if (= k :return)
               k
               this))
            java.lang.Object
            (toString [this]
              "hola")))

(-> obj :foo :bar :return)
(-> obj :foo :bar str)
