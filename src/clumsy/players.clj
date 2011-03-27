; interface to data storage
(ns clumsy.players)

(def players (atom
              {1 (atom {:id 1 :name "p1" :password "a" :room-id 1})
               2 (atom {:id 2 :name "p2" :password "a" :room-id 1})}))



