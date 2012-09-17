;; Copyright (c) 2012 BigML, Inc
;; All rights reserved.

;; Author: Adam Ashenfelter <ashenfad@bigml.com>
;; Start date: Jun 27, 2012

(ns sample.test.core
  (:use clojure.test
        sample.test.util)
  (:require (sample [core :as core]
                    [random :as random])))

(deftest sample
  (is (about-eq (reduce + (take 500 (core/sample (range 1000))))
                250000 25000))
  (is (about-eq (reduce + (take 500 (core/sample (range 1000) :replace true)))
                250000 25000))
  (let [[v1 v2] (vals (frequencies (take 1000 (core/sample [0 1] :replace true))))]
    (is (about-eq v1 v2 150))))

(deftest regression
  (is (= (take 10 (core/sample (range 20) :seed :foo))
         '(7 3 9 6 10 4 2 8 5 13)))
  (is (= (take 10 (core/sample (range 20) :seed 7))
         '(16 13 17 12 9 4 18 7 14 19)))
  (is (= (take 10 (core/sample (range 20) :seed 7 :replace true))
         '(16 4 5 4 0 14 8 9 10 14))))

(defn- make-weighted-data [& {:keys [seed]}]
  (let [rnd (random/create seed)]
    (map list (range) (repeatedly #(Math/abs (.nextGaussian rnd))))))

(deftest weighted-regression
  (let [data (take 10 (make-weighted-data :seed :foo))]
    (= (core/weighted-sample data :seed :bar)
       '(7 0 1 9 8 3 6 4 2 5))
    (= (take 10 (core/weighted-sample data :seed :bar :replace true))
       '(7 0 8 0 0 0 0 9 4 9))))
