;; Copyright (c) 2012 BigML, Inc
;; All rights reserved.

;; Author: Adam Ashenfelter <ashenfad@bigml.com>
;; Start date: Jul 30, 2012

(ns sample.test.stream
  (:use clojure.test
        sample.test.util)
  (:require (sample [stream :as stream])))

(deftest sample
  (is (about-eq (reduce + (stream/sample (range 1000) 500 1000))
                250000 25000))
  (is (about-eq (reduce + (stream/sample (range 1000) 500 1000 :replace true))
                250000 25000))
  (is (about-eq (reduce + (stream/sample (range 1000) 500 1000
                                         :replace true
                                         :rate true))
                250000 35000)))

(deftest regression
  (is (= (stream/sample (range) 10 20 :seed 7)
         '(3 4 5 7 10 12 14 15 16 17)))
  (is (= (stream/sample (range) 10 20 :seed 7 :out-of-bag true)
         '(0 1 2 6 8 9 11 13 18 19)))
  (is (= (stream/sample (range) 10 20 :seed 7 :replace true)
         '(0 1 3 4 7 9 11 16 18 19)))
  (is (= (stream/sample (range) 10 20 :seed 7 :replace true :out-of-bag true)
         '(2 5 6 8 10 12 13 14 15 17)))
  (is (= (stream/sample (range 20) 10 20 :seed 7 :replace true :rate true)
         '(0 1 3 4 7 9 9 10 11 16 19)))
  (is (= (stream/sample (range 20) 10 20
                        :seed 7 :replace true
                        :rate true :out-of-bag true)
         '(2 5 6 8 12 13 14 15 17 18)))
  (let [sum1 (atom 0)
        sum2 (atom 0)]
    (stream/multi-sample (range)
                            [(partial swap! sum1 +) 150 200 :seed 3]
                            [(partial swap! sum2 +) 150 200 :seed 7 :replace true])
    (= '(14557 15921)
       '(@sum1 @sum2)
       (stream/multi-reduce (range)
                            [+ 0 150 200 :seed 3]
                            [+ 0 150 200 :seed 7 :replace true]))))
