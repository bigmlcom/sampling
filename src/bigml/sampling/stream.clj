;; Copyright 2013 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.sampling.stream
  "Provides streaming sampling.  Neither the input population or the
   resulting sample are kept in memory.  The order of the sample is
   not randomized, but will be in the order of the input population."
  (:require (bigml.sampling [random :as random]
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
    (repeat (occurrence/roll sample-size pop-size :rnd rnd) val)))

(defn- without-replacement [val sample-size pop-size rnd]
  (when (and (pos? sample-size) (pos? pop-size))
    (if (> sample-size (random/next-int! rnd pop-size))
      (list val)
      '())))

(defn- create [sample-size pop-size
               & {:keys [seed generator replace rate out-of-bag weigh]}]
  (if weigh
    (throw (Exception. "Weighting not yet supported."))
    (let [state (atom {:sample-size sample-size
                       :pop-size pop-size
                       :rnd (random/create :seed seed :generator generator)})
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
          (if (and out-of-bag (pos? pop-size))
            (if (empty? sample) (list val) '())
            sample))))))

(defn sample
  "Returns a lazy sequence of samples from the collection.  The size
   of the desired sample and the size of the input collection must be
   known ahead of time.  The sample will be in order of the input,
   but this means neither the input stream or the sample need to be
   kept in memory.

   Options:
    :replace - True to sample with replacement, defaults to false.
    :seed - A seed for the random number generator, defaults to nil.
    :generator - The random number generator to be used, options
                 are :lcg (linear congruential) or :twister (Marsenne
                 twister), default is :lcg.
    :rate - When false, the sample result will be sample-size chosen
            from population-size.  When true, each item in the
            population is independently sampled according to the
            probability sample-size / population-size.  Default is
            false.
    :out-of-bag - Returns the out-of-bag items.  Default is false."
  [coll sample-size pop-size & opts]
  (apply concat
         (take-while identity
                     (map (apply create sample-size pop-size opts)
                          coll))))

(defn- multi-stream [coll opts-list]
  (take-while #(some identity %)
              (map (apply juxt (map #(apply create %) opts-list))
                   coll)))

(defn multi-sample
  "multi-sample expects a collection followed by one or more sets of
   sample parameters, each defining a unique sampling of the
   population.

   Each set of sample parameters should be composed of a consumer fn,
   sample size, the population size, and optionally the ':replace',
   ':seed', 'generator', and ':rate' parameters.  See the
   documentation for 'sample' for more about the parameters.

   multi-sample will create a unique set of samples for every
   parameter set.  Whenever a value is sampled, it will be consumed by
   the parameter set's consumer fn.  A consumer fn should accept a
   single parameter.

   Example: (multi-sample (range) [#(println :foo %) 2 5]
                                  [#(println :bar %) 4 5 :replace true])"
  [coll & opts-list]
  (when (seq opts-list)
    (let [consumers (map first opts-list)]
      (doseq [samples (multi-stream coll (map next opts-list))]
        (dorun (map (comp dorun map) consumers samples))))))

(defn multi-reduce
  "multi-reduce expects a collection followed by one or more sets of
   sample parameters, each defining a unique sampling of the
   population.

   Each set of sample parameters should be composed of a reduce fn, an
   initial reduce value, the sample size, the population size, and
   optionally the ':replace', ':seed', 'generator', and ':rate'
   parameters.  See the documentation for 'sample' for more about the
   parameters.

   multi-reduce will create a reduction over the unique set of samples
   for every parameter set.  Whenever a value is sampled, it will be
   reduced by the parameter set's reducer fn.  A reducer fn should
   accept two parameters.

   Example: (multi-reduce (range) [+ 0 2 5]
                                  [- 100 4 5 :replace true])"
  [coll & opts-list]
  (when (seq opts-list)
    (let [reducers (map first opts-list)]
      (reduce #(doall (map reduce reducers %1 %2))
              (map second opts-list)
              (multi-stream coll (map #(drop 2 %) opts-list))))))

(defn- clause-evaluator [clauses]
  (fn [item]
    (if-let [sampler (second (first (drop-while #(not ((first %) item))
                                                clauses)))]
      (sampler item)
      (list))))

(defn cond-sample
  "cond-sample expects a collection followed by pairs of clauses and
   sample definitions.  A clause should be a function that accepts an
   item and returns either true of false.  After each clause should
   follow a sample defition that describes the sampling technique to
   use when the condition is true.

   The sample definition should be composed of the sample size, the
   population size, and optionally the ':replace', ':seed',
   'generator', and ':rate' parameters.  See the documentation for
   'sample' for more about the parameters.

   Example: (cond-sample (range 100)
                         #(< % 50) [2 50]
                         #(>= % 50) [4 50])"
  [coll & clauses]
  (if (odd? (count clauses))
    (throw (Exception. "cond-sample requires sampling options for each clause fn"))
    (mapcat (clause-evaluator (map (fn [[c s]] [c (apply create s)])
                                   (partition 2 clauses)))
            coll)))
