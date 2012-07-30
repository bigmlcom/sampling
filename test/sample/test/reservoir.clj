;; Copyright (c) 2012 BigML, Inc
;; All rights reserved.

;; Author: Adam Ashenfelter <ashenfad@bigml.com>
;; Start date: Jul 30, 2012

(ns sample.test.reservoir
  (:use clojure.test
        sample.test.util)
  (:require (sample [reservoir :as reservoir])))

(deftest sample
  (is (about-eq (reduce + (reservoir/sample (range 1000) 500))
                250000 25000))
  (is (about-eq (reduce + (reservoir/sample (range 1000) 500 :replace true))
                250000 25000))
  (is (= (reservoir/sample (range 20) 10 :seed 7)
         (reduce reservoir/insert
                 (reservoir/create 10 :seed 7)
                 (range 20))))
  (is (= (reservoir/sample (range 20) 10 :seed 7 :replace true)
         (reduce reservoir/insert
                 (reservoir/create 10 :seed 7 :replace true)
                 (range 20)))))

(deftest regression
  (is (= (reservoir/sample (range 20) 10 :seed 7)
         [9 16 11 2 8 19 17 6 15 10]))
  (is (= (reservoir/sample (range 20) 10 :seed 7 :replace true)
         [13 10 9 16 7 2 15 17 4 14])))
