;; Copyright 2013, 2014 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.sampling.reservoir.insertion
  "Provides random sampling using reservoirs.  Uses an insertion
   method that might originally be from Chao's 'A general purpose
   unequal probability sampling plan'.  It's behind a paywall,
   however, so that remains a mystery to me."
  (:require (bigml.sampling [simple :as simple]
                            [random :as random]
                            [occurrence :as occurrence]))
  (:import (bigml.sampling.reservoir.mergeable MergeableReservoir)))

(defmulti ^:private insert
  (fn [reservoir _]
    (if (:indices (meta reservoir))
      ::with-replacement
      ::without-replacement)))

(defmethod insert ::with-replacement [reservoir val]
  (let [{:keys [size insert-count seed generator indices]}
        (meta reservoir)
        insert-count (inc insert-count)
        rnd (random/create :seed seed :generator generator)
        occurrences (occurrence/roll size
                                     insert-count
                                     :seed (random/next-long! rnd)
                                     :generator generator)]
    (with-meta (if (empty? reservoir)
                 (vec (repeat occurrences val))
                 (reduce #(assoc %1 %2 val)
                         reservoir
                         (take occurrences
                               (simple/sample indices
                                              :seed (random/next-long! rnd)
                                              :generator generator))))
      {:size size
       :insert-count insert-count
       :indices indices
       :seed (random/next-long! rnd)
       :generator generator})))

(defmethod insert ::without-replacement [reservoir val]
  (let [{:keys [size insert-count seed generator indices]}
        (meta reservoir)
        insert-count (inc insert-count)
        rnd (random/create :seed seed :generator generator)
        index (random/next-int! rnd insert-count)]
    (with-meta (cond (<= insert-count size)
                     (let [reservoir (conj reservoir val)]
                       (assoc reservoir
                         index val
                         (dec insert-count) (reservoir index)))
                     (< index size)
                     (assoc reservoir index val)
                     :else reservoir)
      {:size size
       :insert-count insert-count
       :seed (random/next-long! rnd)
       :generator generator})))

(declare create)

(deftype Reservoir [reservoir mdata]
  clojure.lang.IPersistentCollection
  (count [_] (count reservoir))
  (seq [_] (seq reservoir))
  (cons [_ i] (Reservoir. (insert reservoir i) mdata))
  (empty [_] (Reservoir. (with-meta [] (assoc (meta reservoir)
                                         :insert-count 0))
                           mdata))
  (equiv [_ i]
    (and (instance? Reservoir i)
         (= reservoir (.reservoir ^Reservoir i))))
  MergeableReservoir
  (mergeReservoir [_ i]
    (let [^Reservoir i i
          {:keys [insert-count size seed] :as rmeta} (meta reservoir)
          weight-items (fn [r]
                         (let [ic (:insert-count (meta r))]
                           (map #(list % ic) r)))
          rsample (simple/sample (concat (weight-items reservoir)
                                         (weight-items (.reservoir i)))
                                 :weigh second
                                 :seed seed)]
      (Reservoir. (with-meta (vec (take size (map first rsample)))
                    (assoc rmeta
                      :insert-count (+ (:insert-count (meta (.reservoir i)))
                                       insert-count)))
                  mdata)))
  clojure.lang.ISeq
  (first [_] (first reservoir))
  (more [_] (Reservoir. (rest reservoir) mdata))
  (next [_] (when-let [r (next reservoir)] (Reservoir. r mdata)))
  java.util.List
  (iterator [_]
    (let [r (atom reservoir)]
      (reify java.util.Iterator
        (next [_] (let [i (first @r)]
                    (swap! r next)
                    i))
        (hasNext [_] (boolean (seq @r))))))
  (toArray [_] (to-array reservoir))
  clojure.lang.IObj
  (meta [_] mdata)
  (withMeta [_ mdata] (Reservoir. reservoir mdata)))

(defn create
  "Creates a sample reservoir given the reservoir size.

   Options:
    :replace - True to sample with replacement, defaults to false.
    :seed - A seed for the random number generator, defaults to nil.
    :generator - The random number generator to be used, options
                 are :lcg (linear congruential) or :twister (Marsenne
                 twister), default is :lcg."
  [size & {:keys [replace seed generator weigh]}]
  (if weigh
    (throw (Exception. "Insertion reservoir does not support weights."))
    (Reservoir. (with-meta [] {:size size
                               :insert-count 0
                               :seed seed
                               :generator generator
                               :indices (when replace
                                          (vec (range size)))})
                nil)))

(defn sample
  "Returns a reservoir sample for a collection given a reservoir size.

   Options:
    :replace - True to sample with replacement, defaults to false.
    :seed - A seed for the random number generator, defaults to nil.
    :generator - The random number generator to be used, options
                 are :lcg (linear congruential) or :twister (Marsenne
                 twister), default is :lcg."
  [coll size & opts]
  (into (apply create size opts) coll))
