(ns clumsy.input-test
  (:use clumsy.input clojure.test
        [clojure.set :only (subset?)]))

(deftest test-get-fns
  (is (= {:a 1 :b 2} (get-fns 1 "b" [(fn [n1 n2] {:a 1}) (fn [n1 n2] {:b 2})]))))

; test that strings, functions and keywords work in a prompt
(deftest prompt-works
  (is (= "1 ." (prompt {:a 1} :a " " (fn [p] ".")))))

(deftest all-rooms-connected
  (is (subset? (keys @rooms) (set (flatten (map vals (vals room-conns)))))))
