(ns clumsy.prompt
  (:use [clojure.contrib.math :only (round)]))

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
  [player & xs] (build-prompt player "" (or (first xs) (:prompt player) default-prompt))
  )

;(defn prompt
;  ([player & [x & xs]] (prompt player (or (:prompt player) default-prompt)))
;  ([player xs] (build-prompt player "" xs))
;  )

; making sure a function will destructure its arg list
;(defn foo [[x :as xs]] (str "x:" x "xs:" xs))
