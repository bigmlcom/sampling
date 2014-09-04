;; Copyright 2013, 2014 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.sampling.test.simple
  (:require [clojure.test :refer :all]
            [bigml.sampling.test.util :refer :all]
            (bigml.sampling [simple :as simple]
                            [random :as random])))

(deftest sample
  (is (about-eq (reduce + (take 500 (simple/sample (range 1000))))
                250000 25000))
  (is (about-eq (reduce + (take 500 (simple/sample (range 1000) :replace true)))
                250000 25000))
  (let [[v1 v2] (vals (frequencies (take 1000 (simple/sample [0 1] :replace true))))]
    (is (about-eq v1 v2 150))))

(deftest regression
  (is (= (take 10 (simple/sample (range 20) :seed :foo))
         '(11 3 15 17 7 8 10 6 18 14)))
  (is (= (take 10 (simple/sample (range 20) :seed 7))
         '(12 1 9 13 18 15 7 0 17 2)))
  (is (= (take 10 (simple/sample (range 20) :seed 7 :replace true))
         '(12 10 13 7 19 15 13 17 1 8))))

(defn- make-weighted-data [& {:keys [seed]}]
  (let [rnd (random/create :seed seed)]
    (map list (range) (repeatedly #(Math/abs (.nextGaussian rnd))))))

(deftest weighted-regression
  (let [data (take 10 (make-weighted-data :seed :foo))]
    (is (= (map first (simple/sample data :seed :bar :weigh second))
           '(2 9 1 4 8 5 7 6 3 0)))
    (is (= (take 10 (map first (simple/sample data
                                            :seed :bar
                                            :weigh second
                                            :replace true)))
           '(2 1 1 4 8 4 8 1 9 4)))))

(deftest twister-regression
  (is (= (take 10 (simple/sample (range 20)
                               :seed 7
                               :generator :twister))
         '(16 10 15 5 7 18 17 3 8 2)))
  (is (= (take 10 (simple/sample (range 20) :seed 7
                               :generator :twister
                               :replace true))
         '(16 10 14 3 4 18 15 17 1 12))))

(deftest zero-weight
  (is (= {:heads 100}
         (->> (simple/sample [:heads :tails]
                             :replace true
                             :weigh {:heads 1 :tails 0})
              (take 100)
              (frequencies)))))
