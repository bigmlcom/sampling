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

(defn- rate-distribution [sample-size pop-size]
  (apply sorted-map
         (mapcat list
                 (occurrence/cumulative-probabilities sample-size
                                                      pop-size)
                 (range))))

(defn- with-replacement-rate [val dist rnd]
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
      '())))

(defn- create [sample-size pop-size & {:keys [seed replace rate]}]
  (let [state (atom {:sample-size sample-size
                     :pop-size pop-size
                     :rnd (random/create (or seed (rand)))})
        dist (when (and replace rate)
               (rate-distribution sample-size pop-size))]
    (fn [val]
      (let [{:keys [sample-size pop-size rnd]} @state
            sample (cond (and replace rate)
                         (with-replacement-rate val dist rnd)
                         replace
                         (with-replacement val sample-size pop-size rnd)
                         :else
                         (without-replacement val sample-size pop-size rnd))]
        (swap! state merge
               {:pop-size (if rate pop-size (dec pop-size))
                :sample-size (if rate
                               sample-size
                               (- sample-size (count sample)))})
        sample))))

(defn sample
  "Returns a lazy sequence of samples from the collection.  The size
   of the desired sample and the size of the input collection must be
   known ahead of time.  The sample will be in order of the input,
   but this means neither the input stream or the sample need to be
   kept in memory.

   Options:
    :replace - True to sample with replacement, defaults to false.
    :seed - A seed for the random number generator, defaults to nil.
    :rate - When false, the sample result will be sample-size chosen
            from population-size.  When true, each item in the
            population is independently sampled according to the
            probability sample-size / population-size.  Default is
            false."
  [coll sample-size pop-size & opts]
  (apply concat
         (take-while identity
                     (map (apply create sample-size pop-size opts)
                          coll))))

(defn multi-sample!
  "multi-sample! expects a collection followed by one or more sets of
   sample parameters, each defining a unique sampling of the
   population.

   Each set of sample parameters should be composed of a consumer fn,
   sample size, the population size, and optionally the ':replace',
   ':seed', and ':rate' parameters.  See the documentation for
   'sample' for more about the parameters.

   multi-sample! will create a unique set of samples for every
   parameter set.  Whenever a value is sampled, it will be consumed by
   the parameter set's consumer fn.  A consumer fn should accept a
   single parameter.

   Example: (multi-sample (range) [#(println :foo %) 2 5]
                                  [#(println :bar %) 4 5 :replace true])"
  [coll & opts-list]
  (when (seq opts-list)
    (let [consumers (map first opts-list)
          stream (take-while #(some identity %)
                             (map (apply juxt (map #(apply create %)
                                                   (map next opts-list)))
                                  coll))]
      (doseq [samples stream]
        (doall (map (fn [consumer vals]
                      (doseq [v vals]
                        (consumer v)))
                    consumers
                    samples))))))
