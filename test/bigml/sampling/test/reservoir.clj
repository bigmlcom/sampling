;; Copyright 2013, 2014 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.sampling.test.reservoir
  (:require [clojure.test :refer :all]
            [bigml.sampling.test.util :refer :all]
            (bigml.sampling [reservoir :as reservoir])))

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
         [18 14 3 7 12 10 1 2 17 6]))
  (is (= (vec (reservoir/sample (range 20) 10 :seed 7 :replace true))
         [3 17 2 14 12 3 3 8 3 14]))
  (is (= (vec (reservoir/sample (range 20) 10 :seed 7
                                :implementation :insertion))
         [11 13 16 10 9 19 17 7 6 14]))
  (is (= (vec (reservoir/sample (range 20) 10 :seed 7 :replace true
                                :implementation :insertion))
         [3 12 17 4 12 11 17 5 5 15])))

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
