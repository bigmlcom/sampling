;; Copyright (c) 2012 BigML, Inc
;; All rights reserved.

;; Author: Adam Ashenfelter <ashenfad@bigml.com>
;; Start date: Jul 2, 2012

(ns sample.test.occurrence
  (:use clojure.test)
  (:require (sample [occurrence :as occurrence])))

(deftest roll
  (= 4 (occurrence/roll 50 10 12345))
  (= 1 (occurrence/roll 50 10 654321)))

(deftest cumulative-prob
  (is (= (occurrence/cumulative-probabilities 4 2)
         '(0.0625 0.3125 0.6875 0.9375 1.0)))
  (is (= (occurrence/cumulative-probabilities 4 2 0.1)
         '(0.0625 0.3125 0.6875 1.0))))

(deftest choose
  (is (== (occurrence/choose-exact 700 35)
          154464913185441564865672312688100541602793485042125471847060N))
  (is (== (occurrence/choose-fast 700 35)
          1.5446491318544158E59))

  (is (.isNaN (occurrence/choose-fast 10000 9990)))
  (is (== (occurrence/choose-exact 10000 9990)
          2743355077591282538231819720749000N))

  (is (== (occurrence/choose 700 35)
          1.5446491318544158E59))
  (is (== (occurrence/choose 10000 9990)
          2743355077591282538231819720749000N))

  (is (== 0 (occurrence/choose 9 10)))
  (is (== 1 (occurrence/choose 10000 10000))))
