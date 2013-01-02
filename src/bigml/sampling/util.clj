;; Copyright 2013 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.sampling.util
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
