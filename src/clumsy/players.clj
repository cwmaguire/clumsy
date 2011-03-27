; interface to data storage
(ns clumsy.players)

(def players (ref {1 {:id 1 :name "p1" :password "a" :room-id 1}
                   2 {:id 2 :name "p2" :password "a" :room-id 1}}))



