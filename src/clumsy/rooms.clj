(ns clumsy.rooms
  (:use [clojure.string :only (join)]
        clumsy.players))

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
                 1 {"west" 2 "street" 2 "east" 4 "hall" 4}
                 2 {"east" 1 "inn" 1 "north" 3}
                 3 {"south" 2}
                 4 {"west" 1 "common" 1 "north" 6 "101" 6 "up" 5 "stairs" 5 "south" 7 "102" 7}
                 5 {"down" 4 "stairs" 4 "north" 8 "201" 8}
                 6 {"south" 4 "hall" 4}
                 7 {"north" 4 "hall" 4}
                 8 {"south" 5 "hall" 5}
  })

(defn move-player
  "dummy command to test moving players"
  [player-id room-id]
  (future
    (let [old-room-id (get-in @players [player-id :room-id])]
      (dosync
       (alter players assoc-in [player-id :room-id] room-id)
       (alter rooms assoc-in [room-id :players] (conj (get-in @rooms [room-id :players]) player-id))
       (alter rooms assoc-in [old-room-id :players] (remove #(= % player-id) (get-in @rooms [old-room-id :players])))))))

(defn room-cmds
  "find the players room and return a map of each matching \"exit\" name
  and a command that will move the player to room at that exit."
  [player-id cmd]
  (let [room-id (:room-id (get @clumsy.players/players player-id))
        ptrn (re-pattern (str "\\b" cmd ".*?\\b"))
        match-conns-fn (fn [kv] (re-find ptrn (get kv 0)))
        room-conn-map (get room-conns room-id)
        entries (filter match-conns-fn room-conn-map)]
    (apply merge {} (flatten
                     (map (fn [kv]
                            {(first kv)
                             (partial move-player player-id (second kv))})
                          entries)))))
