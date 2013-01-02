;; Copyright 2013 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.sampling.test.occurrence
  (:use clojure.test)
  (:require (bigml.sampling [occurrence :as occurrence])))

(def big-result
  1498231660179642550080525374062985229379154060073454416056804436265250417504978421344703666672011193783194306251922106632531575096104465752579970958417306283423558722428981480592122380206679550814874547016793880384420005011964284022150602938812288536154567998961655336231440060094535026560416077739589623596000N)

(deftest roll
  (= 4 (occurrence/roll 50 10 :seed 12345))
  (= 1 (occurrence/roll 50 10 :seed 654321)))

(deftest cumulative-prob
  (is (= (occurrence/cumulative-probabilities 4 2)
         '(0.0625 0.3125 0.6875 0.9375 1.0)))
  (is (= (occurrence/cumulative-probabilities 4 2 0.1)
         '(0.0625 0.3125 0.6875 1.0))))

(deftest choose
  (is (== (occurrence/choose-exact 700 35)
          154464913185441564865672312688100541602793485042125471847060N))
  (is (== (occurrence/choose-fast 700 35)
          1.544649131854415E59))

  (is (.isInfinite (occurrence/choose-fast 10000 9865)))
  (is (== (occurrence/choose-exact 10000 9865)
          big-result))

  (is (== (occurrence/choose 700 35)
          1.544649131854415E59))
  (is (== (occurrence/choose 10000 9865)
          big-result))

  (is (== 0 (occurrence/choose 9 10)))
  (is (== 1 (occurrence/choose 10000 10000))))
