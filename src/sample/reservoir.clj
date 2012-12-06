;; Copyright (c) 2012 BigML, Inc
;; All rights reserved.

;; Author: Adam Ashenfelter <ashenfad@bigml.com>
;; Start date: Jun 22, 2012

(ns sample.reservoir
  "Provides random sampling using reservoirs. This is useful when the
   original population can't be kept in memory but the sample set
   can."
  (:require (sample.reservoir [efraimidis :as efraimidis]
                              [insertion :as insertion])))

(def ^:private implementations
  {:efraimdis efraimidis/create
   :insertion insertion/create})

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
             Defaults to nil.
    :implementation - Chooses the reservoir implementation.  Options
                      are :efraimdis or :insertion.  :insertion may be
                      faster for small populations, but the default
                      is :efraimdis."
  [size & {:keys [weigh implementation] :as opts
           :or {implementation :efraimdis}}]
  (if-let [create-impl (implementations implementation)]
    (apply create-impl size (flatten (seq opts)))
    (throw (Exception. (str "Unknown reservoir implementation "
                            implementation)))))

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
             Defaults to nil.
    :implementation - Chooses the reservoir implementation.  Options
                      are :efraimdis or :insertion.  :insertion may be
                      faster for small populations, but the default
                      is :efraimdis."
  [coll size & opts]
  (into (apply create size opts) coll))
