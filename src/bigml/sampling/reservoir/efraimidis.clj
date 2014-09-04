;; Copyright 2013 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.sampling.reservoir.efraimidis
  "Provides weighted random sampling using reservoirs as described by
   Efraimidis and Spirakis.
   http://utopia.duth.gr/~pefraimi/research/data/2007EncOfAlg.pdf"
  (:require (bigml.sampling [random :as random]
                            [util :as util])
            (clojure.data [finger-tree :as tree]))
  (:import (bigml.sampling.reservoir.mergeable MergeableReservoir)))

(def ^:private compare-k
  #(compare (:k %1) (:k %2)))

(defn- calc-r [rnd & [floor]]
  (let [r (random/next-double! rnd)]
    (if floor
      (+ floor (* r (- 1 floor)))
      r)))

(defn- calc-k [item r weigh]
  (if weigh
    (let [w (weigh item)]
      (if (zero? w)
        0
        (Math/pow r (/ 1 w))))
    r))

(defn- calc-x [reservoir r]
  (/ (Math/log r)
     (Math/log (:k (first reservoir)))))

(defprotocol ^:private CountedSetReservoir
  (getCountedSet [a]))

(deftype Reservoir [reservoir res-size seed gen weigh r wt jmp mdata]
  clojure.lang.IPersistentCollection
  (count [_] (count reservoir))
  (seq [_] (seq (map :item reservoir)))
  (cons [_ i]
    (let [reservoir-count (count reservoir)
          next-wt (+ wt (if weigh (weigh i) 1))]
      (cond (= reservoir-count (dec res-size))
            (let [rnd (random/create :seed seed :generator gen)
                  k (calc-k i (random/next-double! rnd) weigh)
                  reservoir (conj reservoir {:item i :k k})
                  r (random/next-double! rnd)
                  x (calc-x reservoir r)
                  seed (random/next-long! rnd)]
              (Reservoir. reservoir res-size seed gen weigh r 0 x mdata))
            (< reservoir-count res-size)
            (let [rnd (random/create :seed seed :generator gen)
                  k (calc-k i (random/next-double! rnd) weigh)
                  reservoir (conj reservoir {:item i :k k})
                  seed (random/next-long! rnd)]
              (Reservoir. reservoir res-size seed gen weigh nil 0 0 mdata))
            (> next-wt jmp)
            (let [rnd (random/create :seed seed :generator gen)
                  current-thresh (:k (first reservoir))
                  low-r (Math/pow current-thresh (if weigh (weigh i) 1))
                  lthr (Math/pow current-thresh next-wt)
                  hthr (Math/pow current-thresh wt)
                  r2 (/ (- r lthr)
                        (- hthr lthr))
                  r3 (+ low-r (* r2 (- 1 low-r)))
                  k (calc-k i r3 weigh)
                  reservoir (conj (next reservoir) {:item i :k k})
                  r (random/next-double! rnd)
                  x (calc-x reservoir r)
                  seed (random/next-long! rnd)]
              (Reservoir. reservoir res-size seed gen weigh r 0 x mdata))
            :else
            (Reservoir. reservoir res-size seed gen weigh r next-wt jmp mdata))))
  (empty [_] (Reservoir. (tree/counted-sorted-set-by compare-k)
                         res-size seed gen weigh nil 0 0 mdata))
  (equiv
    [_ i]
    (and (instance? Reservoir i)
         (= (into [] reservoir)
            (into [] (.reservoir ^Reservoir i)))
         (= res-size (.res-size ^Reservoir i))
         (= seed (.seed ^Reservoir i))
         (= gen (.gen ^Reservoir i))
         (= weigh (.weigh ^Reservoir i))))
  clojure.lang.ISeq
  (first [_] (:item (first reservoir)))
  (more [_] (Reservoir. (rest reservoir) res-size seed gen weigh nil 0 0 mdata))
  (next [_] (if-let [r (next reservoir)]
              (Reservoir. r res-size seed gen weigh nil 0 0 mdata)))
  MergeableReservoir
  (mergeReservoir [_ i]
    (let [reservoir (into reservoir (.reservoir ^Reservoir i))]
      (Reservoir. (->> (nthnext reservoir (max (- (count reservoir) res-size) 0))
                       (into (empty reservoir)))
                  res-size seed gen weigh r
                  (+ wt (.wt ^Reservoir i))
                  (+ jmp (.jmp ^Reservoir i))
                  mdata)))
  CountedSetReservoir
  (getCountedSet [_] reservoir)
  java.util.List
  (iterator [_]
    (let [r (atom reservoir)]
      (reify java.util.Iterator
        (next [_] (let [i (:item (first @r))]
                    (swap! r next)
                    i))
        (hasNext [_] (boolean (seq @r))))))
  (toArray [_] (to-array (map :item reservoir)))
  clojure.lang.IObj
  (meta [_] mdata)
  (withMeta [_ mdata]
    (Reservoir. reservoir res-size seed gen weigh r wt jmp mdata)))

(defn- init-replacement-reservoir [res-size seed gen weigh]
  (let [rnd (random/create :seed seed :generator gen)]
    (vec (repeatedly res-size
                     #(Reservoir. [] 1 (random/next-double! rnd) gen weigh
                                  nil 0 0 nil)))))

(deftype ReplacementReservoir [reservoir res-size seed gen weigh mdata]
  clojure.lang.IPersistentCollection
  (count [_] (count reservoir))
  (seq [_] (remove nil? (mapcat seq reservoir)))
  (cons [_ i]
    (ReplacementReservoir. (mapv #(conj % i) reservoir)
                           res-size seed gen weigh mdata))
  (empty [_]
    (ReplacementReservoir. (init-replacement-reservoir res-size seed gen weigh)
                           res-size seed gen weigh mdata))
  (equiv [_ i]
    (and (instance? ReplacementReservoir i)
         (= reservoir (.reservoir ^ReplacementReservoir i))
         (= res-size (.res-size ^ReplacementReservoir i))
         (= seed (.seed ^ReplacementReservoir i))
         (= gen (.gen ^ReplacementReservoir i))
         (= weigh (.weigh ^ReplacementReservoir i))))
  clojure.lang.ISeq
  (first [_] (ffirst reservoir))
  (more [_] (ReplacementReservoir. (rest reservoir) res-size seed gen weigh mdata))
  (next [_] (when-let [r (next reservoir)]
              (ReplacementReservoir. r res-size seed gen weigh mdata)))
  MergeableReservoir
  (mergeReservoir [_ i]
    (let [r (->> (concat reservoir (.reservoir ^ReplacementReservoir i))
                 (sort-by #(:k (first (.getCountedSet ^Reservoir %))) >)
                 (take res-size)
                 (vec))]
      (ReplacementReservoir. r res-size seed gen weigh mdata)))
  java.util.List
  (iterator [_]
    (let [r (atom reservoir)]
      (reify java.util.Iterator
        (next [_] (let [i (ffirst @r)]
                    (swap! r next)
                    i))
        (hasNext [_] (boolean (seq @r))))))
  (toArray [_] (to-array (mapcat seq reservoir)))
  clojure.lang.IObj
  (meta [_] mdata)
  (withMeta [_ mdata]
    (ReplacementReservoir. reservoir res-size seed gen weigh mdata)))

(defn create
  "Creates a sample reservoir given the reservoir size.

   Options:
    :replace - True to sample with replacement, defaults to false.
    :seed - A seed for the random number generator, defaults to nil.
    :generator - The random number generator to be used, options
                 are :lcg (linear congruential) or :twister (Marsenne
                 twister), default is :lcg.
    :weigh - A function that returns a non-negative weight for an
             item.  When nil, no sampling weights are applied.
             Defaults to nil."
  [size & {:keys [seed replace generator weigh]}]
  (let [weigh (util/validated-weigh weigh)]
    (if replace
      (ReplacementReservoir. (init-replacement-reservoir size seed generator weigh)
                             size seed generator weigh nil)
      (Reservoir. (tree/counted-sorted-set-by compare-k)
                  size seed generator weigh nil 0 0 nil))))

(defn sample
  "Returns a reservoir sample for a collection given a reservoir size.

   Options:
    :replace - True to sample with replacement, defaults to false.
    :seed - A seed for the random number generator, defaults to nil.
    :generator - The random number generator to be used, options
                 are :lcg (linear congruential) or :twister (Marsenne
                 twister), default is :lcg.
    :weigh - A function that returns a non-negative weight for an
             item.  When nil, no sampling weights are applied.
             Defaults to nil."
  [coll size & opts]
  (into (apply create size opts) coll))
