;; Copyright (c) 2012 BigML, Inc
;; All rights reserved.

;; Author: Adam Ashenfelter <ashenfad@bigml.com>
;; Start date: Jun 21, 2012

(ns sample.stream
  "Provides streaming sampling.  Neither the input population or the
   resulting sample are kept in memory.  The order of the sample is
   not randomized, but will be in the order of the input population."
  (:require (sample [random :as random]
                    [occurrence :as occurrence])))

(defn- approximate-distribution [sample-size pop-size]
  (apply sorted-map
         (mapcat list
                 (occurrence/cumulative-probabilities sample-size
                                                      pop-size)
                 (range))))

(defn- with-replacement-approx [val dist rnd]
  (repeat (second (first (subseq dist >= (random/next-double! rnd))))
          val))

(defn- with-replacement [val sample-size pop-size rnd]
  (when (and (pos? sample-size) (pos? pop-size))
    (repeat (occurrence/roll sample-size
                             pop-size
                             (random/next-seed! rnd))
            val)))

(defn- without-replacement [val sample-size pop-size rnd]
  (when (and (pos? sample-size) (pos? pop-size))
    (if (> sample-size (random/next-int! rnd pop-size))
      (list val)
      (list))))

(defn create
  "Creates a fn that accepts a single value and returns a list
   containing 0 or more samples of the value (> 1 samples are possible
   when sampling with replacement).  The fn includes side effects.
   Each call will alter its internal state and change the results of
   future calls.

   Options:
    :replace - True to sample with replacement, defaults to false.
    :seed - A seed for the random number generator, defaults to nil.
    :approximate - When true, the sample size will be near, but not
                   exactly, the requested size. This is only for
                   sampling with replacement, the default is false."
  [sample-size pop-size & {:keys [seed replace approximate]}]
  (let [state (atom {:sample-size sample-size
                     :pop-size pop-size
                     :seed (or seed (rand))})
        dist (when (and replace approximate)
               (approximate-distribution sample-size pop-size))]
    (fn [val]
      (let [{:keys [sample-size pop-size seed]} @state
            rnd (random/create seed)
            sample (cond (and replace approximate)
                         (with-replacement-approx val dist rnd)
                         replace
                         (with-replacement val sample-size pop-size rnd)
                         :else
                         (without-replacement val sample-size pop-size rnd))]
        (swap! state merge
               {:seed (random/next-seed! rnd)
                :pop-size (if approximate pop-size (dec pop-size))
                :sample-size (if approximate
                               sample-size
                               (- sample-size (count sample)))})
        sample))))

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
  [coll sample-size pop-size & opts]
  (apply concat
         (take-while identity
                     (map (apply create sample-size pop-size opts)
                          coll))))
