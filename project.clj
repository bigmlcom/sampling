(defproject bigml/sampling "3.1"
  :description "Random Sampling in Clojure"
  :url "https://github.com/bigmlcom/sampling"
  :license {:name "Apache License, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :aliases {"lint" ["do" "check," "eastwood"]
            "distcheck" ["do" "clean," "lint," "test"]}
  :profiles {:dev {:plugins [[jonase/eastwood "0.2.3"]]}}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.finger-tree "0.0.2"]
                 [incanter/parallelcolt "0.9.4"]]
  :aot [bigml.sampling.reservoir.mergeable])
