(ns clumsy.rooms
  (:use [clojure.string :only (join)]
        clumsy.players
        clumsy.commands
        clumsy.prompt))

(def rooms (ref {
                 1 {:id 1 :name "Common Room" :desc "The common room of the Frogleg Inn" :items [1]}
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

(def items (ref {
                 1 {:id 1 :name "sword" :desc "a rusty shortsword" :dmg 4}
                 2 {:id 2 :name "bread" :desc "a loaf of bread" :dmg 0}
                 }))

(defn desc-room [player-id]
  (let [{:keys [name desc]} (->> player-id (get @players) :room-id (get @rooms))]
    (msg player-id "Room: " name "\n" desc)))

(defn move-player
  "Update the player room-id, remove player from old room, add to new room"
  [player-id room-id & args]
  (future
    (let [old-room-id (get-in @players [player-id :room-id])]
      (dosync
       (alter players assoc-in [player-id :room-id] room-id)
       (alter rooms assoc-in [room-id :players] (conj (get-in @rooms [room-id :players]) player-id))
       (alter rooms assoc-in [old-room-id :players] (remove #(= % player-id) (get-in @rooms [old-room-id :players])))))
    (desc-room player-id)
    (prompt (get @players player-id))))

(defn get-room-cmds
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

; find all the items in the room that match the first argument
(defn get-item-id [player-id & args]
  (let [ptrn (re-pattern (str "^" (first args)))]
    ( ->> player-id
          (get @players)
          :room-id
          (get @rooms)
          :items
          (select-keys @items)
          vals
          (filter (fn [m] (re-find ptrn (:name m)))))))

(defn get-item [player-id & args]
  "gets an item from the room and associates it with a player"
  (if-let [item-id (apply get-item-id player-id args)]
    (let [player (get @players player-id)
          room-id (:room-id player)]
      (dosync
       (alter players assoc-in [player-id :items] (conj (get-in @players [player-id :items]) item-id))
       (alter rooms assoc-in [room-id :items] (remove #(= % item-id) (get-in @rooms [room-id :items])))))))

(defn get-item-cmds
  [player-id cmd])
