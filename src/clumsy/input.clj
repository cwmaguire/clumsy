(ns clumsy.input
  (:require clumsy.commands)
  (:use [clojure.string :only (split lower-case)]))

(def allowed [""])

(defn exec
  "wrap all but the first word of cmd-str in extra quotes (e.g. \"\\\"arg1\\\"\") and
  then call load-string with a string containing the first word, the player-id and the
  rest of \"quote-wrapped\" or \"double-double-quoted\" strings"
  [player-id cmd-str]
  (let [[cmd & args] (re-seq #"\w+" (lower-case cmd-str))]
    (load-string (apply str (concat [cmd " " player-id] (map #(str "\"" % "\"") args)))))
  )