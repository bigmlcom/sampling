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
    :seed - A seed for the random number generator, defaults to nil.
    :generator - The random number generator to be used, options
                 are :lcg (linear congruential) or :twister (Marsenne
                 twister), default is :lcg."
  [coll & opts]
  (if-not (vector? coll)
    (apply sample (vec coll) opts)
    (let [{:keys [replace]} opts
          sample-fn (if replace
                      with-replacement
                      without-replacement)]
      (sample-fn coll (apply random/create opts)))))

(def ^:private branch-factor 8)

(defn- make-tree [coll]
  (if (> (count coll) branch-factor)
    (let [branch-size (Math/ceil (/ (count coll) branch-factor))
          children (doall (map make-tree (partition-all branch-size coll)))]
      {:total (reduce + (map :total children))
       :children children})
    {:total (reduce + (map second coll))
     :items coll}))

(declare pop-item)

(defn- process-parent [roll [prev-total result item] child]
  (let [new-total (+ prev-total (:total child))]
    (if (and (nil? item)
             (> new-total roll prev-total))
      (let [[new-child item] (pop-item child (- roll prev-total))]
        [(+ prev-total (:total new-child))
         (if (zero? (:total new-child))
           result
           (cons new-child result))
         item])
      [new-total (cons child result) item])))

(defn- process-leaf [roll [prev-total result item] candidate-item]
  (let [[val weight] candidate-item
        new-total (+ prev-total weight)]
    (if (and (nil? item)
             (> new-total roll prev-total))
      [prev-total result val]
      [new-total (cons candidate-item result) item])))

(defn- pop-item [{:keys [total children items]} roll]
  (if children
    (let [[new-total new-children item]
          (reduce (partial process-parent roll) [0] children)]
      [{:total new-total :children new-children} item])
    (let [[new-total new-items item]
          (reduce (partial process-leaf roll) [0] items)]
      [{:total new-total :items new-items} item])))

(defn- weighted-without-replacement [tree rnd]
  (when (pos? (:total tree))
    (let [[new-tree item]
          (pop-item tree (random/next-double! rnd (:total tree)))]
      (cons item (lazy-seq (weighted-without-replacement new-tree rnd))))))

(defn- weighted-with-replacement [coll rnd]
  (let [sm (into (sorted-map)
                 (next (reductions (fn [[tw] [i w]] [(+ tw w) i])
                                   [0]
                                   coll)))
        total (first (last sm))]
    (repeatedly
     #(second (first (subseq sm > (random/next-double! rnd total)))))))

(defn weighted-sample
  "Returns a lazy sequence of samples from a collection of tuples.
   Each tuple should contain an item and a sample weight.  For
   example:

   (weighted-sample [[:heads 0.5] [:tails 0.5]])
   (weighted-sample {:heads 0.5 :tails 0.5})

   This function constructs a tree to index the items in the
   population. This initialization takes O(nlogn) where n is the
   population size. Each sample takes (logn) time.

   Options:
    :replace - True to sample with replacement, defaults to false.
    :seed - A seed for the random number generator, defaults to nil.
    :generator - The random number generator to be used, options
                 are :lcg (linear congruential) or :twister (Marsenne
                 twister), default is :lcg."
  [coll & opts]
  (let [rnd (apply random/create opts)
        coll (random/shuffle! (seq coll) rnd)
        {:keys [replace]} opts]
    (if replace
      (weighted-with-replacement coll rnd)
      (weighted-without-replacement (make-tree coll) rnd))))
