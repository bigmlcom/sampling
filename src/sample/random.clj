;; Copyright (c) 2012 BigML, Inc
;; All rights reserved.

;; Author: Adam Ashenfelter <ashenfad@bigml.com>
;; Start date: Jun 21, 2012

(ns sample.random
  "Fns for creating and using a random number generator. Seeding is
   optional."
  (:import (java.util Random)))

(defn ^Random create
  "Creates a random number generator with an optional seed. Any
   hashable value is valid as a seed."
  ([] (create (rand)))
  ([seed] (Random. (hash (or seed (rand))))))

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
