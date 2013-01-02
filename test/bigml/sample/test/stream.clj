;; Copyright 2013 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.sample.test.stream
  (:use clojure.test
        bigml.sample.test.util)
  (:require (bigml.sample [stream :as stream])))

(deftest sample
  (is (about-eq (reduce + (stream/sample (range 1000) 500 1000))
                250000 25000))
  (is (about-eq (reduce + (stream/sample (range 1000) 500 1000 :replace true))
                250000 25000))
  (is (about-eq (reduce + (stream/sample (range 1000) 500 1000
                                         :replace true
                                         :rate true))
                250000 40000)))

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

(declare iris-data)

(deftest cond-test
  (let [r (stream/cond-sample iris-data
                              #(= (last %) "Iris-setosa") [4 50]
                              #(= (last %) "Iris-versicolor") [2 50]
                              #(= (last %) "Iris-virginica") [1 50])]
    (is (= 7 (count r)))
    (is (= 4 (count (filter #(= (last %) "Iris-setosa") r))))
    (is (= 2 (count (filter #(= (last %) "Iris-versicolor") r))))
    (is (= 1 (count (filter #(= (last %) "Iris-virginica") r)))))
  (let [sample-size 100000
        r (take sample-size (stream/cond-sample (repeatedly #(rand))
                                                #(>= % 0.5) [1 2 :rate true]
                                                #(< % 0.5) [1 4 :rate true]))]
    (is (about-eq (count (filter #(>= % 0.5) r))
                  (* 2 (count (filter #(< % 0.5) r)))
                  (/ sample-size 50)))))

(def iris-data
  "The well known iris dataset."
  [[5.1 3.5 1.4 0.2 "Iris-setosa"] [4.9 3.0 1.4 0.2 "Iris-setosa"]
   [4.7 3.2 1.3 0.2 "Iris-setosa"] [4.6 3.1 1.5 0.2 "Iris-setosa"]
   [5.0 3.6 1.4 0.2 "Iris-setosa"] [5.4 3.9 1.7 0.4 "Iris-setosa"]
   [4.6 3.4 1.4 0.3 "Iris-setosa"] [5.0 3.4 1.5 0.2 "Iris-setosa"]
   [4.4 2.9 1.4 0.2 "Iris-setosa"] [4.9 3.1 1.5 0.1 "Iris-setosa"]
   [5.4 3.7 1.5 0.2 "Iris-setosa"] [4.8 3.4 1.6 0.2 "Iris-setosa"]
   [4.8 3.0 1.4 0.1 "Iris-setosa"] [4.3 3.0 1.1 0.1 "Iris-setosa"]
   [5.8 4.0 1.2 0.2 "Iris-setosa"] [5.7 4.4 1.5 0.4 "Iris-setosa"]
   [5.4 3.9 1.3 0.4 "Iris-setosa"] [5.1 3.5 1.4 0.3 "Iris-setosa"]
   [5.7 3.8 1.7 0.3 "Iris-setosa"] [5.1 3.8 1.5 0.3 "Iris-setosa"]
   [5.4 3.4 1.7 0.2 "Iris-setosa"] [5.1 3.7 1.5 0.4 "Iris-setosa"]
   [4.6 3.6 1.0 0.2 "Iris-setosa"] [5.1 3.3 1.7 0.5 "Iris-setosa"]
   [4.8 3.4 1.9 0.2 "Iris-setosa"] [5.0 3.0 1.6 0.2 "Iris-setosa"]
   [5.0 3.4 1.6 0.4 "Iris-setosa"] [5.2 3.5 1.5 0.2 "Iris-setosa"]
   [5.2 3.4 1.4 0.2 "Iris-setosa"] [4.7 3.2 1.6 0.2 "Iris-setosa"]
   [4.8 3.1 1.6 0.2 "Iris-setosa"] [5.4 3.4 1.5 0.4 "Iris-setosa"]
   [5.2 4.1 1.5 0.1 "Iris-setosa"] [5.5 4.2 1.4 0.2 "Iris-setosa"]
   [4.9 3.1 1.5 0.2 "Iris-setosa"] [5.0 3.2 1.2 0.2 "Iris-setosa"]
   [5.5 3.5 1.3 0.2 "Iris-setosa"] [4.9 3.6 1.4 0.1 "Iris-setosa"]
   [4.4 3.0 1.3 0.2 "Iris-setosa"] [5.1 3.4 1.5 0.2 "Iris-setosa"]
   [5.0 3.5 1.3 0.3 "Iris-setosa"] [4.5 2.3 1.3 0.3 "Iris-setosa"]
   [4.4 3.2 1.3 0.2 "Iris-setosa"] [5.0 3.5 1.6 0.6 "Iris-setosa"]
   [5.1 3.8 1.9 0.4 "Iris-setosa"] [4.8 3.0 1.4 0.3 "Iris-setosa"]
   [5.1 3.8 1.6 0.2 "Iris-setosa"] [4.6 3.2 1.4 0.2 "Iris-setosa"]
   [5.3 3.7 1.5 0.2 "Iris-setosa"] [5.0 3.3 1.4 0.2 "Iris-setosa"]
   [7.0 3.2 4.7 1.4 "Iris-versicolor"] [6.4 3.2 4.5 1.5 "Iris-versicolor"]
   [6.9 3.1 4.9 1.5 "Iris-versicolor"] [5.5 2.3 4.0 1.3 "Iris-versicolor"]
   [6.5 2.8 4.6 1.5 "Iris-versicolor"] [5.7 2.8 4.5 1.3 "Iris-versicolor"]
   [6.3 3.3 4.7 1.6 "Iris-versicolor"] [4.9 2.4 3.3 1.0 "Iris-versicolor"]
   [6.6 2.9 4.6 1.3 "Iris-versicolor"] [5.2 2.7 3.9 1.4 "Iris-versicolor"]
   [5.0 2.0 3.5 1.0 "Iris-versicolor"] [5.9 3.0 4.2 1.5 "Iris-versicolor"]
   [6.0 2.2 4.0 1.0 "Iris-versicolor"] [6.1 2.9 4.7 1.4 "Iris-versicolor"]
   [5.6 2.9 3.6 1.3 "Iris-versicolor"] [6.7 3.1 4.4 1.4 "Iris-versicolor"]
   [5.6 3.0 4.5 1.5 "Iris-versicolor"] [5.8 2.7 4.1 1.0 "Iris-versicolor"]
   [6.2 2.2 4.5 1.5 "Iris-versicolor"] [5.6 2.5 3.9 1.1 "Iris-versicolor"]
   [5.9 3.2 4.8 1.8 "Iris-versicolor"] [6.1 2.8 4.0 1.3 "Iris-versicolor"]
   [6.3 2.5 4.9 1.5 "Iris-versicolor"] [6.1 2.8 4.7 1.2 "Iris-versicolor"]
   [6.4 2.9 4.3 1.3 "Iris-versicolor"] [6.6 3.0 4.4 1.4 "Iris-versicolor"]
   [6.8 2.8 4.8 1.4 "Iris-versicolor"] [6.7 3.0 5.0 1.7 "Iris-versicolor"]
   [6.0 2.9 4.5 1.5 "Iris-versicolor"] [5.7 2.6 3.5 1.0 "Iris-versicolor"]
   [5.5 2.4 3.8 1.1 "Iris-versicolor"] [5.5 2.4 3.7 1.0 "Iris-versicolor"]
   [5.8 2.7 3.9 1.2 "Iris-versicolor"] [6.0 2.7 5.1 1.6 "Iris-versicolor"]
   [5.4 3.0 4.5 1.5 "Iris-versicolor"] [6.0 3.4 4.5 1.6 "Iris-versicolor"]
   [6.7 3.1 4.7 1.5 "Iris-versicolor"] [6.3 2.3 4.4 1.3 "Iris-versicolor"]
   [5.6 3.0 4.1 1.3 "Iris-versicolor"] [5.5 2.5 4.0 1.3 "Iris-versicolor"]
   [5.5 2.6 4.4 1.2 "Iris-versicolor"] [6.1 3.0 4.6 1.4 "Iris-versicolor"]
   [5.8 2.6 4.0 1.2 "Iris-versicolor"] [5.0 2.3 3.3 1.0 "Iris-versicolor"]
   [5.6 2.7 4.2 1.3 "Iris-versicolor"] [5.7 3.0 4.2 1.2 "Iris-versicolor"]
   [5.7 2.9 4.2 1.3 "Iris-versicolor"] [6.2 2.9 4.3 1.3 "Iris-versicolor"]
   [5.1 2.5 3.0 1.1 "Iris-versicolor"] [5.7 2.8 4.1 1.3 "Iris-versicolor"]
   [6.3 3.3 6.0 2.5 "Iris-virginica"] [5.8 2.7 5.1 1.9 "Iris-virginica"]
   [7.1 3.0 5.9 2.1 "Iris-virginica"] [6.3 2.9 5.6 1.8 "Iris-virginica"]
   [6.5 3.0 5.8 2.2 "Iris-virginica"] [7.6 3.0 6.6 2.1 "Iris-virginica"]
   [4.9 2.5 4.5 1.7 "Iris-virginica"] [7.3 2.9 6.3 1.8 "Iris-virginica"]
   [6.7 2.5 5.8 1.8 "Iris-virginica"] [7.2 3.6 6.1 2.5 "Iris-virginica"]
   [6.5 3.2 5.1 2.0 "Iris-virginica"] [6.4 2.7 5.3 1.9 "Iris-virginica"]
   [6.8 3.0 5.5 2.1 "Iris-virginica"] [5.7 2.5 5.0 2.0 "Iris-virginica"]
   [5.8 2.8 5.1 2.4 "Iris-virginica"] [6.4 3.2 5.3 2.3 "Iris-virginica"]
   [6.5 3.0 5.5 1.8 "Iris-virginica"] [7.7 3.8 6.7 2.2 "Iris-virginica"]
   [7.7 2.6 6.9 2.3 "Iris-virginica"] [6.0 2.2 5.0 1.5 "Iris-virginica"]
   [6.9 3.2 5.7 2.3 "Iris-virginica"] [5.6 2.8 4.9 2.0 "Iris-virginica"]
   [7.7 2.8 6.7 2.0 "Iris-virginica"] [6.3 2.7 4.9 1.8 "Iris-virginica"]
   [6.7 3.3 5.7 2.1 "Iris-virginica"] [7.2 3.2 6.0 1.8 "Iris-virginica"]
   [6.2 2.8 4.8 1.8 "Iris-virginica"] [6.1 3.0 4.9 1.8 "Iris-virginica"]
   [6.4 2.8 5.6 2.1 "Iris-virginica"] [7.2 3.0 5.8 1.6 "Iris-virginica"]
   [7.4 2.8 6.1 1.9 "Iris-virginica"] [7.9 3.8 6.4 2.0 "Iris-virginica"]
   [6.4 2.8 5.6 2.2 "Iris-virginica"] [6.3 2.8 5.1 1.5 "Iris-virginica"]
   [6.1 2.6 5.6 1.4 "Iris-virginica"] [7.7 3.0 6.1 2.3 "Iris-virginica"]
   [6.3 3.4 5.6 2.4 "Iris-virginica"] [6.4 3.1 5.5 1.8 "Iris-virginica"]
   [6.0 3.0 4.8 1.8 "Iris-virginica"] [6.9 3.1 5.4 2.1 "Iris-virginica"]
   [6.7 3.1 5.6 2.4 "Iris-virginica"] [6.9 3.1 5.1 2.3 "Iris-virginica"]
   [5.8 2.7 5.1 1.9 "Iris-virginica"] [6.8 3.2 5.9 2.3 "Iris-virginica"]
   [6.7 3.3 5.7 2.5 "Iris-virginica"] [6.7 3.0 5.2 2.3 "Iris-virginica"]
   [6.3 2.5 5.0 1.9 "Iris-virginica"] [6.5 3.0 5.2 2.0 "Iris-virginica"]
   [6.2 3.4 5.4 2.3 "Iris-virginica"] [5.9 3.0 5.1 1.8 "Iris-virginica"]])
