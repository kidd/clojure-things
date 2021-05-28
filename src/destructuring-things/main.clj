;; https://gist.github.com/john2x/e1dca953548bfdfb9844
;; https://blog.brunobonacci.com/2014/11/16/clojure-complete-guide-to-destructuring/
(ns destructuring-things.main)

(defn check-params [{:keys [params user ctx], context :ctx, :as request}]
  (= ctx context))
