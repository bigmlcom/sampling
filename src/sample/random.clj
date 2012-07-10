;; Copyright (c) 2012 BigML, Inc
;; All rights reserved.

;; Author: Adam Ashenfelter <ashenfad@bigml.com>
;; Start date: Jun 21, 2012

(ns sample.random
  "Fns for creating and using a random number generator. Seeding is
   optional."
  (:import (java.util Random)))

(defn ^Random create
  "Creates a random number generator with an optional seed"
  [& [^long seed]]
  (if seed (Random. seed) (Random.)))

(defn next-seed!
  "Returns a new seed given a random number generator."
  [^Random rnd]
  (.nextLong rnd))

(defn next-int!
  "Returns an integer given a random number generator and a range."
  [^Random rnd ^long range]
  (.nextInt rnd range))

(defn next-double!
  "Returns a double given a random number generator."
  [^Random rnd]
  (.nextDouble rnd))

(defn shuffle!
  "Shuffles a collection given a random number generator.  Adapted
   from the clojure.core/shuffle."
  [^java.util.Collection coll ^Random rnd]
  (let [al (java.util.ArrayList. coll)]
    (java.util.Collections/shuffle al rnd)
    (clojure.lang.RT/vector (.toArray al))))
