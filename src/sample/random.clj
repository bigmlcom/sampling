;; Copyright (c) 2012 BigML, Inc
;; All rights reserved.

;; Author: Adam Ashenfelter <ashenfad@bigml.com>
;; Start date: Jun 21, 2012

(ns sample.random
  "Fns for creating and using a random number generator. Seeding is
   optional."
  (:import (java.util Random)))

(defn ^Random create
  "Creates a random number generator.  Returns a Random when a seed is
   given, or nil (signifying an unseeded generator) when no seed is
   given."
  [& [seed]]
  {:generator (when seed (Random. seed))})

(defn next-seed!
  "Returns a new seed given a random number generator, or nil when the
   generator is unseeded."
  [^Random rnd]
  (when rnd (.nextLong rnd)))

(defn next-int!
  "Returns an integer given a random number generator and a range."
  [^Random rnd range]
  (if rnd (.nextInt rnd range) (rand-int range)))

(defn next-double!
  "Returns a double given a random number generator."
  [^Random rnd]
  (if rnd (.nextDouble rnd) (rand)))

(defn shuffle!
  "Shuffles a collection given a random number generator.  Adapted
   from the clojure.core/shuffle."
  [^java.util.Collection coll ^Random rnd]
  (if rnd
    (let [al (java.util.ArrayList. coll)]
      (java.util.Collections/shuffle al rnd)
      (clojure.lang.RT/vector (.toArray al)))
    (shuffle coll)))
