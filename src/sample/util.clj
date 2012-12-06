;; Copyright (c) 2012 BigML, Inc
;; All rights reserved.

;; Author: Adam Ashenfelter <ashenfad@bigml.com>
;; Start date: Dec 5, 2012

(ns sample.util
  "Provides utility functions.")

(defn validated-weigh
  "Returns a 'weigh' function whose output is validated."
  [weigh]
  (when weigh
    #(let [weight (weigh %)]
       (cond (nil? weight)
             (throw (Exception. (str "Weight is nil." "\n  Item:" %)))
             (not (number? weight))
             (throw (Exception. (str "Weight is not a number."
                                     "\n  Item: " % "\n  Weight: " weight)))
             (neg? weight)
             (throw (Exception. (str "Weight is negative."
                                     "\n  Item: " % "\n  Weight: " weight)))
             :else weight))))
