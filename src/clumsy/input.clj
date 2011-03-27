(ns clumsy.input 
  (:use clumsy.commands)
  (:use clumsy.rooms )
  (:use [clojure.string :only (split lower-case join)]))

(def player-cmd-fns [clumsy.rooms/get-room-cmds clumsy.rooms/get-item-cmds])

(defn match-commands
  "runs each fully qualified function listed in fns with the player-id and cmd;
   merges resulting maps"
  [player-id cmd fns]
  (apply merge (flatten ((apply juxt fns) player-id cmd))))

(defn split-args
  "split args on space or tab"
  [cmd] (re-seq #"[^ \t]+" (lower-case cmd)))

(defn exec
  "split cmd-str by space or tab, search for commands matching first word,
   display message if # of cmds not equal to 1, else run cmd with
   remaining words; commands can be regular expressions:
   e.g. \"n\" \"n.*th\""
  [player-id cmd-str]
  (let [[cmd & args] (split-args cmd-str)
    matched-cmds (match-commands player-id cmd player-cmd-fns)
    num-matched-cmds (count matched-cmds)]
    (cond (= 1 num-matched-cmds) (apply (first (vals matched-cmds)) args)
        (= 0 num-matched-cmds) (msg player-id "No command found matching " cmd)
        :else (msg player-id "Ambiguous: " (apply str (interpose " " (map first matched-cmds)))))))




