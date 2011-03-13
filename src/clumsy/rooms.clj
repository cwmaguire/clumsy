(ns clumsy.rooms)

(def rooms {
  1 {:id 1 :name "Common Room" :desc "The common room of the Frogleg Inn"}
  2 {:id 2 :name "Street" :desc "Outside the Frogleg Inn"}
  3 {:id 3 :name "Street" :desc "Outside the bank"}
  4 {:id 4 :name "hallway" :desc "First floor hallway inside the Frogleg Inn"}
  5 {:id 5 :name "hallway" :desc "Second floor hallway inside the Frogleg Inn"}
  6 {:id 6 :name "Room 101" :desc "A room in the Frogleg Inn"}
  7 {:id 7 :name "Room 102" :desc "A room in the Frogleg Inn"}
  8 {:id 8 :name "Room 201" :desc "A room in the Frogleg Inn"}
  })

(def room-conns {
  [1 2] {1 ["north" "street"] 2 ["west" "inn"]}
  [2 3] {2 ["north"] 3 ["south"]}
  [1 4] {1 ["east" "hall"] 4 ["west" "common"]}
  [4 6] {4 ["north" "101"] 6 ["south" "hall"]}
  [4 7] {4 ["south" "102"] 7 ["north" "hall"]}
  [4 5] {4 ["up" "stairs"] 5 ["down" "stairs"]}
  [5 8] {5 ["north" "201"] 8 ["south" "hall"]}
  })

;get room connection commands: everyone should be able to run those
(defn room-commands []
  (set (flatten (map vals (vals room-conns)))))