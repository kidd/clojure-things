(ns lanterna-thing.lanterna
  (:require [lanterna.screen :as s]))

(def scr (s/get-screen))

(s/start scr)
(s/put-string scr 10 10 "hello world!")
(s/put-string scr 10 11 "Press any key to exit!")
(s/redraw scr)
(s/get-key-blocking scr)

(s/stop scr)
