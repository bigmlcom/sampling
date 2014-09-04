;; Copyright 2013, 2014 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.sampling.random
  "Functions for creating and using a random number generator."
  (:import (cern.jet.random.tdouble.engine MersenneTwister64)
           (java.util Random)))

(defn- make-mersenne-rng [^long seed]
  (let [m (MersenneTwister64. seed)]
    (proxy [java.util.Random] []
      (nextInt
        ([] (.nextInt m))
        ([i] (int (* i (.nextDouble m)))))
      (nextDouble [] (.nextDouble m))
      (nextFloat [] (.nextFloat m)))))

(defn create
  "Creates a random number generator with an optional seed and
   generator.

   Options:
    :seed - Any hashable value, defaults to a random seed.
    :generator - Either lcg (linear congruential) or twister (Mersenne
                 twister), defaults to lcg."
  [& {:keys [seed generator]}]
  (let [seed (hash (or seed (rand)))
        generator (or generator :lcg)]
    (case (keyword generator)
      :lcg (Random. seed)
      :twister (make-mersenne-rng seed)
      (throw (Exception. "Generator must be lcg or twister.")))))

(defn next-double!
  "Returns a double given a random number generator and an optional
   range."
  ([^Random rnd] (.nextDouble rnd))
  ([rnd range] (* range (next-double! rnd))))

(defn next-long!
  "Returns a new seed given a random number generator."
  ([^Random rnd] (.nextLong rnd))
  ([rnd ^long range] (long (* range (next-double! rnd)))))

(defn next-int!
  "Returns an integer given a random number generator and an optional
  range."
  ([^Random rnd] (.nextInt rnd))
  ([^Random rnd ^long range] (.nextInt rnd range)))

(defn shuffle!
  "Shuffles a collection given a random number generator.  Adapted
   from the clojure.core/shuffle."
  [^java.util.Collection coll ^Random rnd]
  (let [al (java.util.ArrayList. coll)]
    (java.util.Collections/shuffle al rnd)
    (clojure.lang.RT/vector (.toArray al))))
