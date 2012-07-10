;; Copyright (c) 2012 BigML, Inc
;; All rights reserved.

;; Author: Adam Ashenfelter <ashenfad@bigml.com>
;; Start date: Jun 22, 2012

(ns sample.reservoir
  "Provides random sampling using reservoirs. This is useful when the
   original population can't be kept in memory but the sample set
   can. Final reservoirs are in random order."
  (:require (sample [core :as core]
                    [random :as random]
                    [occurrence :as occurrence])))

(defn create
  "Creates a sample reservoir given the reservoir size. The reservoir
   is initialized as an empty vector.

   Options:
    :replace - True to sample with replacement, defaults to false.
    :seed - A seed for the random number generator, defaults to nil."
  [reservoir-size & {:keys [seed replace]}]
  (with-meta [] {:reservoir-size reservoir-size
                 :insert-count 0
                 :seed (or seed (random/next-seed! (random/create)))
                 :indices (when replace
                            (vec (range reservoir-size)))}))

(defmulti insert
  "Inserts a value into the sample reservoir (a vector of items)."
  (fn [reservoir _]
    (if (:indices (meta reservoir))
      ::with-replacement
      ::without-replacement)))

(defmethod insert ::with-replacement [reservoir val]
  (let [{:keys [reservoir-size insert-count seed indices]} (meta reservoir)
        insert-count (inc insert-count)
        rnd (random/create seed)
        occurrences (occurrence/roll reservoir-size
                                     insert-count
                                     (random/next-seed! rnd))]
    (with-meta (if (empty? reservoir)
                 (vec (repeat occurrences val))
                 (reduce #(assoc %1 %2 val)
                         reservoir
                         (take occurrences
                               (core/sample indices :seed (random/next-seed! rnd)))))
      {:reservoir-size reservoir-size
       :insert-count insert-count
       :indices indices
       :seed (random/next-seed! rnd)})))

(defmethod insert ::without-replacement [reservoir val]
  [reservoir val]
  (let [{:keys [reservoir-size insert-count seed indices]} (meta reservoir)
        insert-count (inc insert-count)
        rnd (random/create seed)
        index (random/next-int! rnd insert-count)]
    (with-meta (cond (<= insert-count reservoir-size)
                     (let [reservoir (conj reservoir val)]
                       (assoc reservoir
                         index val
                         (dec insert-count) (reservoir index)))
                     (< index reservoir-size)
                     (assoc reservoir index val)
                     :else reservoir)
      {:reservoir-size reservoir-size
       :insert-count insert-count
       :seed (random/next-seed! rnd)})))

(defn sample
  "Returns a reservoir sample (a vector of items) for a collection
   given a reservoir size.

   Options:
    :replace - True to sample with replacement, defaults to false.
    :seed - A seed for the random number generator, defaults to nil."
  [coll reservoir-size & opts]
  (reduce insert (apply create reservoir-size opts)
          coll))
