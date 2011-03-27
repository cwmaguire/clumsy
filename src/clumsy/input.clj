(ns clumsy.input 
  (:use clumsy.commands)
  (:use clumsy.rooms )
  (:use [clojure.string :only (split lower-case join)]))

;(def allowed (room-commands))

(def player-cmd-fns [clumsy.rooms/room-cmds])

;NOTE should return a COMMAND, not a string
(defn match-commands [player-id cmd fns]
  (apply merge (flatten ((apply juxt fns) player-id cmd))))

(defn split-args [cmd] (re-seq #"[^ \t]+" (lower-case cmd)))

;NEEDS TEST, works!
(defn build-quoted-cmd [cmd player-id args]
  (apply str (interpose " " (concat [cmd player-id] (map #(str "\"" % "\"") args)))))

(defn exec
  "wrap all but the first word of cmd-str in extra quotes (e.g. \"\\\"arg1\\\"\") and
  then call load-string with a string containing the first word, the player-id and the
  rest of \"quote-wrapped\" or \"double-double-quoted\" strings"
  [player-id cmd-str]
  (let [[cmd & args] (split-args cmd-str)
    matched-cmds (match-commands player-id cmd player-cmd-fns)
    num-matched-cmds (count matched-cmds)]
    ; We don't need load-string: when we search for a command, any
                                        ; result should be a command, not a string
    ;otherwise we have to then find out what command matches the first
    ;word of the string, and why not do that at the first step? 
    (cond (= 1 num-matched-cmds) (apply (first (vals matched-cmds)) args)
        (= 0 num-matched-cmds) (msg player-id "No command found matching " cmd)
        :else (msg player-id "Ambiguous: " (apply str (interpose " " (map first matched-cmds)))))))




