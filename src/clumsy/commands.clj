(ns clumsy.commands)

(defn msg [player-id & strs]
  (print "[" player-id "] " strs))
