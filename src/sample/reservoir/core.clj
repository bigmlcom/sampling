(ns sample.reservoir.core
  "Provides common code for reservoir implementations.")

(defprotocol MergeableReservoir
  (mergeReservoir [a b]))
