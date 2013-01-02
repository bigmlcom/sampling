;; Copyright 2013 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.sampling.occurrence
  "Provides functions for computing the number of occurrences to be
   expected for an item in a population when sampled with
   replacement."
  (:import (cern.jet.math.tdouble DoubleArithmetic))
  (:require (bigml.sampling [random :as random])))

(def default-probability-cutoff
  "The cumulative-probabilities fn will stop calculating occurrence
   probabilities when they drop below this threshold."
  1E-10)

(defn ^Double choose-fast
  "Fast but approximate and unsafe calculation for n choose k."
  [n k]
  (DoubleArithmetic/binomial (double n) (long k)))

(defn- choose-exact* [n k acc]
  (cond (zero? k) acc
        (zero? n) 0N
        :else (recur (dec n) (dec k) (* acc (/ n k)))))

(defn choose-exact
  "Safe and exact but slow calculation for n choose k."
  [n k]
  (choose-exact* n k 1N))

(defn choose
  "Calculates n choose k. Attemps a fast approximate calculation with
  an exact calculation as a fail over."
  [n k]
  (if (>= n k)
    (let [result (choose-fast n k)]
      (if (or (.isInfinite result)
              (.isNaN result))
        (choose-exact n k)
        result))
    0))

(defn- occurrence-prob [sample-size pop-size occurrences]
  (let [select-prob (/ 1.0 pop-size)]
    (* (Math/pow select-prob occurrences)
       (Math/pow (- 1.0 select-prob) (- sample-size occurrences))
       (choose sample-size occurrences))))

(defn- sim-occurrences [sample-size pop-size rnd]
  (let [sample-prob (/ (double pop-size))]
    (count (filter true? (repeatedly sample-size
                                     #(> sample-prob (random/next-double! rnd)))))))

(defn cumulative-probabilities
  "Returns a list of cumulative probabilities in order of
   occurrence. The list will stop when the occurrence probability
   drops below the optional prob-cutoff (default 1E-10)."
  [sample-size pop-size & [prob-cutoff]]
  (let [prob-cutoff (or prob-cutoff default-probability-cutoff)]
    (concat (take-while
             (partial > (- 1 prob-cutoff))
             (reductions + (map (partial occurrence-prob sample-size pop-size)
                                (range))))
            [1.0])))

(defn roll
  "Roll the number of occurrences for an item given sample size,
   population size, and either an optional random number generator or
   instead a seed and generator type."
  [sample-size pop-size & opts]
  (let [{:keys [rnd]} opts
        rnd (or rnd (apply random/create opts))]
    ;; Simulate the occurrences when it becomes numerically difficult
    ;; to compute them directly
    (if (> (/ sample-size (double pop-size)) 50)
      (sim-occurrences sample-size pop-size rnd)
      (count (take-while (partial > (random/next-double! rnd))
                         (cumulative-probabilities sample-size pop-size))))))
