;; Copyright 2013 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns sample.test.simple
  (:use clojure.test
        sample.test.util)
  (:require (sample [simple :as simple]
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
         '(7 3 9 6 10 4 2 8 5 13)))
  (is (= (take 10 (simple/sample (range 20) :seed 7))
         '(16 13 17 12 9 4 18 7 14 19)))
  (is (= (take 10 (simple/sample (range 20) :seed 7 :replace true))
         '(16 4 5 4 0 14 8 9 10 14))))

(defn- make-weighted-data [& {:keys [seed]}]
  (let [rnd (random/create :seed seed)]
    (map list (range) (repeatedly #(Math/abs (.nextGaussian rnd))))))

(deftest weighted-regression
  (let [data (take 10 (make-weighted-data :seed :foo))]
    (is (= (map first (simple/sample data :seed :bar :weigh second))
           '(9 0 8 3 1 4 7 6 5 2)))
    (is (= (take 10 (map first (simple/sample data
                                            :seed :bar
                                            :weigh second
                                            :replace true)))
           '(9 6 9 4 0 8 4 1 3 0)))))

(deftest twister-regression
  (is (= (take 10 (simple/sample (range 20)
                               :seed 7
                               :generator :twister))
         '(5 9 6 3 10 17 12 18 8 2)))
  (is (= (take 10 (simple/sample (range 20) :seed 7
                               :generator :twister
                               :replace true))
         '(5 8 4 0 7 17 9 17 0 6))))
