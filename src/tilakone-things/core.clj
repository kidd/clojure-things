(ns tilakone-things.core
  (:require [tilakone.core :as tk :refer [_]]))



(defn inc-val [val & _]  (inc val))


(def instance-states
  [{::tk/name :starting}
   {::tk/name :running
    ::tk/transitions [{::tk/on :stop! ::tk/to :stopping ::tk/actions [:stop!]}]}
   {::tk/name :stopping
    ::tk/transitions [{::tk/on :really-stop! ::tk/to :stopped  ::tk/actions [:stop!]}]}
   {::tk/name :stopped
    ::tk/transitions [{::tk/on :start! ::tk/to :running}]}
   {::tk/name :deleting}
   {::tk/name :delete-failed}
   {::tk/name :suspended}
   {::tk/name :cancellation-pending}
   {::tk/name :deleted}
   ])


(def instance-fsm
  {::tk/states instance-states
   ::tk/action! (fn [{::tk/keys [action state] :as fsm}]
                  (let [fsm (update fsm :audit conj (str state " => " action))]
                    (case action
                      :stop!
                      (update fsm :count inc))))
   ::tk/state :running
   :count 0
   :audit [:running]})

(-> instance-fsm (tk/apply-signal :stop!)
                          (tk/apply-signal :really-stop!)
                          (tk/apply-signal :start!)
                          (tk/apply-signal :stop!)

                          (update-in [::tk/states 2] conj {::tk/name :fuck ::tk/transitions [{::tk/on :fuck ::tk/to :fuck}]})
                          (tk/apply-signal :fuck))


(def count-ab-states
  [{::tk/name        :start
    ::tk/transitions [{::tk/on \a, ::tk/to :found-a}
                      {::tk/on _}]}
   {::tk/name        :found-a
    ::tk/transitions [{::tk/on \a}
                      {::tk/on \b, ::tk/to :start, ::tk/actions [:inc-val]}
                      {::tk/on _, ::tk/to :start}]}])

; FSM has states, a function to execute actions, and current state and value:

(def count-ab
  {::tk/states  count-ab-states
   ::tk/action! (fn [{::tk/keys [action] :as fsm}]
                  (case action
                    :inc-val (update fsm :count inc)))
   ::tk/state   :start
   :count       0})

; Lets apply same inputs to our FSM:

(->> ["abaaabc" "aaacb" "bbbcab"]
     (map (partial reduce tk/apply-signal count-ab))
     (map :count))
