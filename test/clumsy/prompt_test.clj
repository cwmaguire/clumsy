(ns clumsy.prompt-test
  (:use clumsy.prompt clojure.test))

; test that strings, functions and keywords work in a prompt
(deftest prompt-works
  (is (= "1 ." (prompt {:a 1} [:a " " (fn [p] ".")]))))

(run-tests )
