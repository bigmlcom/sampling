;; Copyright 2013 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.sampling.test.reservoir
  (:use clojure.test
        bigml.sampling.test.util)
  (:require (bigml.sampling [reservoir :as reservoir])))

(deftest sample
  (is (about-eq (reduce + (reservoir/sample (range 1000) 500))
                250000 25000))
  (is (about-eq (reduce + (reservoir/sample (range 1000) 500 :replace true))
                250000 25000))
  (is (= (reservoir/sample (range 20) 10 :seed 7)
         (into (reservoir/create 10 :seed 7)
               (range 20))))
  (is (= (reservoir/sample (range 20) 10 :seed 7 :replace true)
         (into (reservoir/create 10 :seed 7 :replace true)
               (range 20)))))

(deftest regression
  (is (= (vec (reservoir/sample (range 20) 10 :seed 7))
         [8 0 14 16 13 3 4 18 6 5]))
  (is (= (vec (reservoir/sample (range 20) 10 :seed 7 :replace true))
         [19 8 11 5 1 10 15 1 0 15]))
  (is (= (vec (reservoir/sample (range 20) 10 :seed 7
                                :implementation :insertion))
         [9 16 11 2 8 19 17 6 15 10]))
  (is (= (vec (reservoir/sample (range 20) 10 :seed 7 :replace true
                                :implementation :insertion))
         [13 10 9 16 7 2 15 17 4 14])))

(deftest weighting
  (let [result (reservoir/sample [:heads :tails] 4000
                                 :replace true
                                 :weigh {:heads 3 :tails 1})]
    (is (about-eq 3000 (:heads (frequencies result)) 100)))
  (let [result (into (reservoir/create 4000
                                       :replace true
                                       :weigh {:heads 3 :tails 1})
                     [:heads :tails])]
    (is (about-eq 3000 (:heads (frequencies result)) 100))))

(deftest res-merge
  (is (about-eq (reduce + (reservoir/merge
                           (reservoir/sample (range 1300) 500)
                           (reservoir/sample (range 1300 2000) 500)))
                (reduce + (reservoir/sample (range 2000) 500))
                75000))
  (is (about-eq (reduce + (reservoir/merge
                           (reservoir/sample (range 1300) 500)
                           (reservoir/sample (range 1300 2000) 500)))
                (reduce + (reservoir/merge
                           (reservoir/sample (range 1300) 500
                                             :implementation :insertion)
                           (reservoir/sample (range 1300 2000) 500
                                             :implementation :insertion)))
                75000))
  (is (about-eq (reduce + (reservoir/merge
                           (reservoir/sample (range 1300) 500 :replace true)
                           (reservoir/sample (range 1300 2000) 500 :replace true)))
                (reduce + (reservoir/sample (range 2000) 500 :replace true))
                75000))
  (is (about-eq (reduce + (reservoir/merge
                           (reservoir/sample (range 0 5000) 500 :weigh identity)
                           (reservoir/sample (range 5000 8000) 500 :weigh identity)
                           (reservoir/sample (range 8000 10000) 500 :weigh identity)))
                (reduce + (reservoir/sample (range 0 10000) 500 :weigh identity))
                200000)))
