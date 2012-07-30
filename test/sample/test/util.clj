;; Copyright (c) 2012 BigML, Inc
;; All rights reserved.

;; Author: Adam Ashenfelter <ashenfad@bigml.com>
;; Start date: Jul 30, 2012

(ns sample.test.util)

(defn about-eq
  "Returns true if the absolute value of the difference
   between the first two arguments is less than the third."
  [v1 v2 tol]
  (< (Math/abs (double (- v1 v2))) tol))
