(ns clumsy.input 
  (:use [clojure.string :only (split lower-case join)]))


(def players (ref {1 {:id 1 :name "p1" :password "a" :room-id 1}
                   2 {:id 2 :name "p2" :password "a" :room-id 1}}))
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

(defn msg [player-id & strs]
  (print "[" player-id "] " strs))

(defn desc-room [player-id]
  (let [{:keys [name desc]} (->> player-id (get @players) :room-id (get @rooms))]
    (msg player-id "Room: " name "\n" desc)))

(def default-prompt [">"])

(defn eval-prompt
  "if x is a function or a keyword then returns (x player) else x"
  [x player]
  (if (or (fn? x) (keyword? x)) (x player) x))

(defn build-prompt
  "builds a prompt string given a player, an initial prompt and a list of functions, keywords and strings;
  each function will be passed the player and keywords are applied to the player
  e.g. (prompt {:curr-hp 5 :max-hp 20} [\"HP: \":curr-hp \"|\" :max-hp \" (\" (fn [player] (round (* 100 (/ (:curr-hp player) (:max-hp player))))) \"%)\"]) returns \"HP: 5|20 (25%)\""
  [player prompt [x :as xs]]
  (if x
    (recur player (str prompt (eval-prompt x player)) (rest xs))
    prompt))

(defn prompt
  "Calls build-prompt with either the supplied prompt arguments, the player's prompt arguments or some default prompt arguments"
  [player & xs] (build-prompt player "" (cond (not (empty? xs)) xs (:prompt player) (:prompt player) :else default-prompt)))

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
  (let [room-id (:room-id (get @players player-id))
        ptrn (re-pattern (str "\\b" cmd ".*?\\b"))
        match-conns-fn (fn [kv] (re-find ptrn (get kv 0)))
        room-conn-map (get room-conns room-id)
        entries (filter match-conns-fn room-conn-map)]
    (apply merge {} (flatten
                     (map (fn [kv]
                            {(first kv)
                             (partial move-player player-id (second kv))})
                          entries)))))

(defn get-room-item-ids [player-id & args]
  "find all the items in the room that match the first argument"
  (let [ptrn (re-pattern (str "^" (first args)))]
    ( ->> player-id
          (get @players)
          :room-id
          (get @rooms)
          :items
          (select-keys @items)
          vals
          (filter (fn [m] (re-find ptrn (:name m)))))))

(defn get-player-item-ids [player-id & args]
  "find all the items belonging to the player that match the first argument"
  (let [ptrn (re-pattern (str "^" (first args)))]
    ( ->> player-id
          (get @players)
          :items
          (select-keys @items)
          vals
          (filter (fn [m] (re-find ptrn (:name m)))))))

;I was going to have the user disambiguate, but decided to just let
;them pick up more than one item at once
(defn get-item [player-id & args]
  "gets all items from the room and associates them with the player"
  (if-let [items (apply get-room-item-ids player-id args)]
    (let [player (get @players player-id)
          room-id (:room-id player)]
      (dosync
       (alter players assoc-in [player-id :items] (concat (get-in @players [player-id :items]) (map :id items)))
       (alter rooms assoc-in [room-id :items] (remove (fn [x] (some (fn [y] (= x y)) (map :id items)) x) (get-in @rooms [room-id :items])))))))

(defn drop-item [player-id & args]
  "drops all items from the player and associates them with the player's current room"
  (if-let [items (apply get-player-item-ids player-id args)]
    (let [player (get @players player-id)
          room-id (:room-id player)]
      (dosync
       (alter rooms assoc-in [room-id :items] (concat (get-in @rooms [room-id :items]) (map :id items)))
       (alter players assoc-in [player-id :items] (remove (fn [x] (some (fn [y] (= x y)) (map :id items)) x) (get-in @players [player-id :items])))))))

(defn get-item-cmds
  [player-id cmd])

(def player-cmd-fns [get-room-cmds get-item-cmds])

(defn get-fns
  "runs each fully qualified function listed in fns with the player-id and cmd;
   merges resulting maps"
  [player-id cmd fns]
  (apply merge (flatten ((apply juxt fns) player-id cmd))))

;Put up a menu for users to choose between matching commands.
;Allow them to choose either all the functions, one of the functions
;(by number) or none of the functions
(defn disambiguate-cmd
  "given a list of commands, store the commands with the player and prompt the user to pick one, none, or all"
  [fn-map player-id]
                                        ;1, store the functions
                                        ;2. prompt the user to pick
  (dosync (alter players assoc-in [player-id :ambiguous-cmds] fn-map))
  (msg player-id
       "Multiple matches:\n"
       (apply str
              (interleave (concat ["a"] (range 1 (inc (count fn-map))) ["q"])
                          (concat [(apply juxt (vals fn-map))] (vals fn-map) [prompt])))))

; re-use this whenever there is an ambiguity: commands, arguments,
;etc. 
(defn match-freq-dispatch
  "given a map of names to functions and the users original command:
   prompt if there are none, exec if there is one, disambiguate if there is > 1"
  [player-id fn-map cmd args]
  (let [num-fns (count fn-map)]
    (cond (= 1 num-fns) (apply (first (vals fn-map)) args)
          (= 0 num-fns) (msg player-id "No command found matching " cmd)
          :else (disambiguate-cmd fn-map cmd args))))

(defn split-args
  "split args on space or tab"
  [cmd] (re-seq #"[^ \t]+" (lower-case cmd))) 

(defn exec
  "split cmd-str by space or tab, search for commands matching first word,
   display message if # of cmds not equal to 1, else run cmd with
   remaining words; commands can be regular expressions:
   e.g. \"n\" \"n.*th\""
  [player-id cmd-str]
  (let [[cmd & args] (split-args cmd-str) fn-map (get-fns player-id cmd player-cmd-fns)]
    (match-freq-dispatch player-id fn-map cmd args)
))



