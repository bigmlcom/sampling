;; Copyright 2013 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns sample.test.util)

(defn about-eq
  "Returns true if the absolute value of the difference
   between the first two arguments is less than the third."
  [v1 v2 tol]
  (< (Math/abs (double (- v1 v2))) tol))
