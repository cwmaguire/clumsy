(ns clumsy.rooms-test
  (:use clojure.test
        [clojure.set :only (subset?)]
        clumsy.rooms))

(run-tests)

(deftest all-rooms-connected
  (is (subset? (keys @rooms) (set (flatten (map vals (vals room-conns)))))))
