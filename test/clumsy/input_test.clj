(ns clumsy.input-test
  (:use clumsy.input clojure.test))

(deftest test-match-commands
  (is (= {:a 1 :b 2} (match-commands 1 "b" [(fn [n1 n2] {:a 1}) (fn [n1 n2] {:b 2})]))))

