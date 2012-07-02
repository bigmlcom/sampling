;; Copyright (c) 2012 BigML, Inc
;; All rights reserved.

;; Author: Adam Ashenfelter <ashenfad@bigml.com>
;; Start date: Jul 2, 2012

(ns sample.occurrence
  "Provides functions for computing the number of occurrences to be
   expected for an item in a population when sampled with
   replacement."
  (:require (sample [random :as random])))

(def default-probability-cutoff
  "The cumulative-probabilities fn will stop calculating occurrence
   probabilities when they drop below this threshold."
  1E-10)

(defn- choose [a b]
  (/ (reduce * (map double (range (- (inc a) b) (inc a))))
     (reduce * (map double (range 1 (inc b))))))

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
             ;; Stop calculating occurance probabilities once the
             ;; liklihood becomes smaller than the cutoff
             (partial > (- 1 prob-cutoff))
             (reductions + (map (partial occurrence-prob sample-size pop-size)
                                (range))))
            [1.0])))

(defn roll
  "Roll the number of occurrences for an item given sample size,
   population size, and an optional seed."
  [sample-size pop-size & [seed]]
  (let [rnd (random/create seed)]
    ;; Simulate the occurrences when it becomes numerically difficult
    ;; to compute them directly
    (if (> (/ sample-size (double pop-size)) 50)
      (sim-occurrences sample-size pop-size rnd)
      (count (take-while (partial > (random/next-double! rnd))
                         (cumulative-probabilities sample-size pop-size))))))
