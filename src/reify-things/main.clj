;; https://www.braveclojure.com/multimethods-records-protocols/
;; http://web.archive.org/web/20170226082330/http://decomplecting.org/blog/2014/10/29/reify-this/
;; https://stackoverflow.com/questions/4509782/simple-explanation-of-clojure-protocols
;; https://stackoverflow.com/questions/37058268/what-is-reify-in-clojure


;; https://medium.com/@TheLaddersEng/stateful-components-in-clojure-part-1-6e819f51db14


;; reify is to defrecord what fn is to defn.

(ns reify-things.main)

(defprotocol Quacks
  (quack [_] "DOC: something ducky"))

(defprotocol Colorful
  (color [_] "red") )

(defrecord MyRec [name age color]
  Colorful
  (color [_] color))

(color (MyRec. "a" "b" "color"))

(count
 (reify Quacks
   (quack [_] "special quack")
   Colorful
   (color [_] "green!")
   clojure.lang.Counted
   (count [_] 43)))
