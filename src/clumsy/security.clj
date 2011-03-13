(ns clumsy.security)

;I want to control what commands people can run and I also need to map commands like get to clumsy.items/get

;get all the room connection commands

(def player-commands (atom {
  :lulu ["get"]
  }))
