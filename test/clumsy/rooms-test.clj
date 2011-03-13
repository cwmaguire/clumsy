(ns clumsy.rooms-test
  (:use clojure.test
        [clojure.set :only (subset?)]
        clumsy.rooms))

(run-tests)

(deftest all-rooms-connected
  (is (subset? (keys rooms) (set (flatten (keys room-conns))))))
