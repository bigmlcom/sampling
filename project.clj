(defproject bigml/sampling "2.1.0"
  :description "Random Sampling in Clojure"
  :url "http://bigmlcom.github.com/sampling"
  :license {:name "Apache License, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [incanter/parallelcolt "0.9.4"]
                 [org.clojure/data.finger-tree "0.0.1"]]
  :aot [bigml.sampling.reservoir.mergeable])
