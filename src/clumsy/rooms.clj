(ns clumsy.rooms
  (:use [clojure.string :only (join)] clumsy.players))

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
                 3 {["south"] 2}
                 4 {"west" 1 "common" 1 "north" 6 "101" 6 "up" 5 "stairs" 5 "south" 7 "102" 7}
                 5 {"down" 4 "stairs" 4 "north" 8 "201" 8}
                 6 {"south" 4 "hall" 4}
                 7 {"north" 4 "hall" 4}
                 8 {"south" 5 "hall" 5}
  })

;(defn player-room-cmds [player-id cmd]
;  (re-seq (re-pattern (str "\\b" cmd ".*?\\b")) (join " " (flatten (keys (get room-conns (:room (get @players player-id))))))))

(defn move-player [player-id room-id] (println "moving player " player-id " to room " room-id))

; if cmd matches a string in any key of the room connections for the
; players room, then return a command that will move the player to the
; connected room
(defn room-cmds [player-id cmd]
  (let [room-id (:room-id @(get @players player-id))
        ptrn (re-pattern (str "\\b" cmd ".*?\\b"))
        match-conns-fn (fn [kv] (re-find ptrn (get kv 0)))
        room-conn-map (get room-conns room-id)
        entries (filter match-conns-fn room-conn-map)]
    (apply merge {}
           (flatten
            (map (fn [kv]
                   { (first kv)
                     (partial move-player player-id (second kv))}) entries)))))
