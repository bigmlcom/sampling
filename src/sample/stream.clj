;; Copyright (c) 2012 BigML, Inc
;; All rights reserved.

;; Author: Adam Ashenfelter <ashenfad@bigml.com>
;; Start date: Jun 21, 2012
(ns sample.stream
  "Provides streaming sampling.  Neither the input population or the
   resulting sample are kept in memory.  The order of the sample is
   not randomized, but will be in the order of the input population."
  (:require (sample [random :as random]
                    core)))

(defn- choose [a b]
  (/ (reduce * (map double (range (- (inc a) b) (inc a))))
     (reduce * (map double (range 1 (inc b))))))

(defn- occurance-prob [sample-size pop-size occurances]
  (let [select-prob (/ 1.0 pop-size)]
    (* (Math/pow select-prob occurances)
       (Math/pow (- 1.0 select-prob) (- sample-size occurances))
       (choose sample-size occurances))))

(defn- sim-occurances [sample-size pop-size rnd]
  (let [sample-prob (/ (double pop-size))]
    (count (filter true? (repeatedly sample-size
                                     #(> sample-prob (random/next-double! rnd)))))))

(defn- occurance-probs [sample-size pop-size]
  (concat (take-while
           (partial > 0.99999999999)
           (reductions + (map (partial occurance-prob sample-size pop-size)
                              (range))))
          [1.0]))

(defn- roll-occurances [sample-size pop-size seed]
  (let [rnd (random/create seed)]
    (if (> (/ sample-size (double pop-size)) 50)
      (sim-occurances sample-size pop-size rnd)
      (count (take-while (partial > (random/next-double! rnd))
                         (occurance-probs sample-size pop-size))))))

(defn- sample-with-distribution [coll dist rnd]
  (mapcat (fn [val]
            (take (second (first (subseq dist >= (random/next-double! rnd))))
                  (repeat val)))
          coll))

(defn- with-replacement-approx [coll sample-size pop-size rnd]
  (sample-with-distribution (take pop-size coll)
    (apply sorted-map (mapcat list (occurance-probs sample-size pop-size)
                              (range)))
    rnd))

(defn- with-replacement [coll sample-size pop-size rnd]
  (lazy-seq
   (when (and (pos? sample-size) (pos? pop-size))
     (let [occurances (roll-occurances sample-size
                                       pop-size
                                       (random/next-seed! rnd))]
       (concat (repeat occurances (first coll))
               (with-replacement (next coll)
                 (- sample-size occurances)
                 (dec pop-size)
                 rnd))))))

(defn- without-replacement [coll sample-size pop-size rnd]
  (lazy-seq
   (when (and (pos? sample-size) (pos? pop-size))
     (if (> sample-size (random/next-int! rnd pop-size))
       (cons (first coll)
             (without-replacement (next coll) (dec sample-size) (dec pop-size) rnd))
       (without-replacement (next coll) sample-size (dec pop-size) rnd)))))

(defn sample
  "Returns a lazy sequence of samples from the collection.  The size
   of the desired sample and the size of the input collection must be
   known ahead of time.  tTe sample will be in order of the input,
   but this means neither the input stream or the sample need to be
   kept in memory.

   Options:
    :replace - True to sample with replacement, defaults to false.
    :seed - A seed for the random number generator, defaults to nil.
    :approximate - When true, the sample size will be near, but not
                   exactly, the requested size. This is only for
                   sampling with replacement, the default is false."
  [coll sample-size pop-size & {:keys [seed replace approximate]}]
  (let [sample-fn (cond (not replace) without-replacement
                        approximate with-replacement-approx
                        :else with-replacement)]
    (sample-fn coll sample-size pop-size (random/create seed))))
