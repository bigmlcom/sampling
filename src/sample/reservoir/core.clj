;; Copyright 2013 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns sample.reservoir.core
  "Provides common code for reservoir implementations.")

(defprotocol MergeableReservoir
  (mergeReservoir [a b]))
