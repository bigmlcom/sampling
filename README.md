
# Random Sampling in Clojure
============================

This library supports three flavors of random sampling: simple
sampling, reservoir sampling, and stream sampling. As we review each,
feel free to follow along in the REPL:

```clojure
user> (ns test
        (:require (sample [core :as core]
                          [reservoir :as reservoir]
                          [stream :as stream])))
```

## Simple Sampling

`sample.core` provides simple random sampling. With this technique the
original population is kept in memory but the resulting sample set is
produced as a lazy sequence.

By default, sampling is done [without replacement]
(http://www.ma.utexas.edu/users/parker/sampling/repl.htm). This
is equivalent to a lazy [Fisher-Yates shuffle]
(http://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle).

```clojure
test> (core/sample (range 5))
(2 3 1 0 4)
```

Setting `:replace` as true will sample with replacement. Since there
is no limit to the number of items that can be sampled with
replacement from a population, the result will be an infinite length
list.  So make sure to `take` however many samples you need.

```clojure
test> (take 10 (core/sample (range 5) :replace true))
(2 3 3 2 4 1 1 1 3 0)
```

Each call to `core/sample` will return a new sample order.

```clojure
test> (core/sample (range 5))
(0 2 3 1 4)
test> (core/sample (range 5))
(3 1 4 2 0)
```

Setting the `:seed` parameter allows the sample order to be recreated.

```clojure
test> (core/sample (range 5) :seed 7)
(1 3 2 0 4)
test> (core/sample (range 5) :seed 7)
(1 3 2 0 4)
```

Any value that's hashable is valid as a seed:

```clojure
test> (core/sample (range 5) :seed :foo)
(2 1 3 0 4)
```

## Reservoir Sampling

`sample.reservoir` provides functions for [reservoir sampling]
(http://en.wikipedia.org/wiki/Reservoir_sampling). This is best when
the original population is too large to fit into memory or is
streaming so the overall size is unknown. The sample reservoir is kept
in memory as a vector of items.

To create a sample reservoir, use `reservoir/create` and give it the
number of samples you desire. Then use `reservoir/insert` to stream
values through the reservoir. For example:

```clojure
test> (reductions reservoir/insert (reservoir/create 3) (range 10))
([] [0] [0 1] [0 1 2] [0 1 3] [4 1 3] [4 1 3] [4 1 6] [4 1 7] [4 1 7] [4 1 9])
```

For convenience, `reservoir/sample` accepts a collection and a
reservoir size and returns the final reduced reservoir:

```clojure
test> (reservoir/sample (range 10) 5)
[0 9 2 1 4]
```

Both `reservoir/sample` and `reservoir/create` support the `:replace`
and `:seed` parameters.

```clojure
test> (reservoir/sample (range 10) 5 :replace true :seed 3)
[2 5 3 3 8]
```

## Stream Sampling

`sample.stream` is useful when taking a large sample from a large
population. Neither the original population or the resulting sample are
kept in memory. There are a couple of caveats. First, unlike the other
sampling techniques, the resulting sample stream is not in random
order. It will be in the order of the original population. So if you
need a random ordering, you'll want to shuffle the sample. The second
caveat is that, unlike reservoir sampling, the size of the population
must be declared up-front.

To use stream sampling call `stream/sample` with a collection (the
population), the desired number of samples, and the size of the
population. The result is a lazy sequence of samples:

```clojure
test> (stream/sample (range 10) 5 10)
(1 2 4 7 9)
```

As with the other sampling techniques, both `:replace` and `:seed` are
supported by `stream/sample`:

```clojure
test> (stream/sample (range 10) 5 10 :replace true :seed 3)
(2 3 4 5 7)
```

It's computationally expensive to select the exact number of desired
samples when using `stream/sample` with replacement. If you're okay
with the number of samples being *approximately* the desired number,
then you can set `:approximate` to true for much faster performance:

```clojure
test> (time (count (stream/sample (range 10000) 5000 10000 :replace true)))
"Elapsed time: 374.021 msecs"
5000
test> (time (count (stream/sample (range 10000) 5000 10000
                                  :replace true :approximate true)))
"Elapsed time: 33.923 msecs"
4954
```
