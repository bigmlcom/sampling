(defproject sample "2.0.0"
  :description "Random Sampling in Clojure"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [incanter/parallelcolt "0.9.4"]
                 [org.clojure/data.finger-tree "0.0.1"]]
  :aot [sample.reservoir.core])
