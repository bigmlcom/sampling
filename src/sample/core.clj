;; Copyright (c) 2012 BigML, Inc
;; All rights reserved.

;; Author: Adam Ashenfelter <ashenfad@bigml.com>
;; Start date: Jun 21, 2012

(ns sample.core
  "Provides simple random sampling. The original population is kept in
   memory but the resulting sample set is produced as a lazy
   sequence."
  (:require (sample [random :as random])))

(defn- with-replacement [coll rnd]
  (when-not (empty? coll)
    (repeatedly #(nth coll (random/next-int! rnd (count coll))))))

(defn- without-replacement [coll rnd]
  (when-not (empty? coll)
    (let [index (random/next-int! rnd (count coll))]
      (cons (nth coll index)
            (lazy-seq (without-replacement
                       (subvec (assoc coll index (first coll)) 1)
                       rnd))))))

(defn sample
  "Returns a lazy sequence of samples from the collection.  The
   collection is kept in memory.

   Options:
    :replace - True to sample with replacement, defaults to false.
    :seed - A seed for the random number generator, defaults to nil."
  [coll & opts]
  (if-not (vector? coll)
    (apply sample (vec coll) opts)
    (let [{:keys [seed replace]} opts
          sample-fn (if replace
                      with-replacement
                      without-replacement)]
      (sample-fn coll (random/create seed)))))
