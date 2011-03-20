(ns clumsy.input-test
  (:use clumsy.input clojure.test))

(deftest test-match-commands
  (is (= [ "a" "b"] (match-commands 1 "b" [(fn [n1 n2] "a") (fn [n1 n2] "b")]))))

(run-tests)
