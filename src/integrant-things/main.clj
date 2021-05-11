;; https://www.sweettooth.dev/endpoint/dev/architecture/integrant-tutorial.html
;; https://www.youtube.com/playlist?list=PLb5SjnGEsSJdJnbjxVWci1P7mKY-K1ExD
;; https://github.com/prestancedesign/usermanager-reitit-integrant-example/
;; https://www.karimarttila.fi/clojure/2020/09/07/clojure-integrant-exercise.html
;; https://tiagodalloca.github.io/blog/event-driven-coffee-machine/
(ns integrant-things.main
  (:require [integrant.core :as ig]))

(def system-config
  {::a {::b (ig/ref ::b)}
   ::b {}})

(defmethod ig/init-key ::a
  [& args]
  (apply println args))

(defmethod ig/halt-key! ::a
  [& args]
  (apply println args))

(defmethod ig/init-key ::b
  [& args]
  (apply println args)
  :b)

(defmethod ig/halt-key! ::b
  [& args]
  (apply println args))

(defn -main [a]
  (let [system (ig/init system-config)]
    (println "             initialized!")
    (ig/halt! system)))
