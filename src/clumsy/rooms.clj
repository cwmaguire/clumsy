(ns clumsy.rooms
  (:use clumsy.players))

(def rooms (ref {
                 1 {:id 1 :name "Common Room" :desc "The common room of the Frogleg Inn"}
                 2 {:id 2 :name "Street" :desc "Outside the Frogleg Inn"}
                 3 {:id 3 :name "Street" :desc "Outside the bank"}
                 4 {:id 4 :name "hallway" :desc "First floor hallway inside the Frogleg Inn"}
                 5 {:id 5 :name "hallway" :desc "Second floor hallway inside the Frogleg Inn"}
                 6 {:id 6 :name "Room 101" :desc "A room in the Frogleg Inn"}
                 7 {:id 7 :name "Room 102" :desc "A room in the Frogleg Inn"}
                 8 {:id 8 :name "Room 201" :desc "A room in the Frogleg Inn"}
                 }))

(def room-conns {
                 1 {["west" "street"] 2 ["east" "hall"] 4}
                 2 {["east" "inn"] 1 ["north"] 3}
                 3 {["south"] 2}
                 4 {["west" "common"] 1 ["north" "101"] 6 ["up" "stairs"] 5 ["south" "102"] 7}
                 5 {["down" "stairs"] 4 ["north" "201"] 8}
                 6 {["south" "hall"] 4}
                 7 {["north" "hall"] 4}
                 8 {["south" "hall"] 5}
  })

;get room connection commands: everyone should be able to run those
(defn room-commands
  ([]
     (set (flatten (map vals (vals room-conns)))))
  ([player-id]
     (let [func (fn [mp] (get mp (:room (get @players player-id))))]
       (flatten (map func (filter func (vals room-conns)))))))
