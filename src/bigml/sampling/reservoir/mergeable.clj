;; Copyright 2013 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.sampling.reservoir.mergeable
  "Provides the definition for mergeable reservoirs.")

(defprotocol MergeableReservoir
  (mergeReservoir [a b]))
