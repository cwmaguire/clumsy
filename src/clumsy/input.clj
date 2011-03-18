(ns clumsy.input
  (:use clumsy.commands)
  (:use clumsy.rooms)
  (:use [clojure.string :only (split lower-case)]))

(def allowed (room-commands))

(defn match-commands [player-id cmd]
  (filter #(re-find (cons "^" cmd)) (flatten (seq (room-commands player-id)))))

(defn exec
  "wrap all but the first word of cmd-str in extra quotes (e.g. \"\\\"arg1\\\"\") and
  then call load-string with a string containing the first word, the player-id and the
  rest of \"quote-wrapped\" or \"double-double-quoted\" strings"
  [player-id cmd-str]
  (let [[cmd & args] (re-seq #"\w+" (lower-case cmd-str)) matched-cmds (match-commands player-id cmd)]
    (cond (= 1 (count matched-cmds)) (load-string (apply str (concat [cmd " " player-id] (map #(str "\"" % "\"") args))))
          (= 0 (count matched-cmds)) (msg player-id "No command found matching " cmd)
          :else (msg player-id "Ambiguous: " (str matched-cmds))))
  )


