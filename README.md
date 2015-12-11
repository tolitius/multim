# multim

where a lonely key meets multiple values

[![Clojars Project](http://clojars.org/multim/latest-version.svg)](http://clojars.org/multim)

## Why

They come handy. Sometimes. And at those times it's good to have them.

## Time Series

Let's say we have events streaming in in a form of `{timestamp {ticker event-id}}`:

```clojure
(def events [
  [1449088877203 {:ticker :GOOG :event-id 1}]
  [1449088876590 {:ticker :AAPL :event-id 2}]
  [1449088877601 {:ticker :MSFT :event-id 3}]
  [1449088877203 {:ticker :TSLA :event-id 4}]
  [1449088875914 {:ticker :NFLX :event-id 5}]
  [1449088870005 {:ticker :FB   :event-id 6}] ])
```

* we'd like to keep them in the map.
* we'd also like to keep them _sorted_ by time (i.e. timestamp)

_notice_ that Tesla and Google have _the same timestamp_ (i.e. same key value).

## Multi Mode

As events come in they can be added into something like a [TreeMultimap](http://docs.guava-libraries.googlecode.com/git/javadoc/com/google/common/collect/TreeMultimap.html)
which is both: sorted and multimap.

```clojure
;; syntax: (tree-multimap [key-comparator] [value-comparator])

user=> (tree-multimap <)

#object[com.google.common.collect.TreeMultimap 0x1fabbda8 "{}"]
```

a map with no data is interesting, but not as much as a map with the data:

```clojure
user=> (def mm (into-multi 
                 (tree-multimap <) events))

#object[com.google.common.collect.TreeMultimap 0x688a6108
"{1449088877601=[{:ticker :MSFT, :event-id 3}], 
  1449088877203=[{:ticker :GOOG, :event-id 1}, {:ticker :TSLA, :event-id 4}],
  1449088876590=[{:ticker :AAPL, :event-id 2}],
  1449088875914=[{:ticker :NFLX, :event-id 5}],
  1449088870005=[{:ticker :FB, :event-id 6}]}"]
```

_notice_ how it groupped values for the `1449088877203` timestamp.

### Before and After

Since the map is sorted, it should be quite simple to find all the entries before or after certain time. 

#####before
```clojure
user=> (to mm 1449088876592)

{1449088870005 #{{:ticker :FB, :event-id 6}}, 
 1449088875914 #{{:ticker :NFLX, :event-id 5}}, 
 1449088876590 #{{:ticker :AAPL, :event-id 2}}}
```

#####after
```clojure
user=> (from mm 1449088876592)

{1449088877203 #{{:ticker :GOOG, :event-id 1} {:ticker :TSLA, :event-id 4}}, 
 1449088877601 #{{:ticker :MSFT, :event-id 3}}}
```

#### View with a View

While `TreeMultimap` has all the chops, it is mutable, hence it is better to create a navigatable view based on the same `tree-multimap`:

```clojure
user=> (def view (into-view 
                   (tree-multimap <) events))
#'user/view

user=> (type view)
com.google.common.collect.AbstractMapBasedMultimap$NavigableAsMap
```

it would of course be boring if this view type was not extended with a `Sliceable` protocol (as the `TreeMultimap` above):

```clojure
(defprotocol Sliceable 
  (from [this k])
  (to [this k]))
```

so it does extend it as well:

#####before
```clojure
user=> (to view 1449088876592)

{1449088870005 #{{:ticker :FB, :event-id 6}}, 
 1449088875914 #{{:ticker :NFLX, :event-id 5}}, 
 1449088876590 #{{:ticker :AAPL, :event-id 2}}}
```

#####after
```clojure
user=> (from view 1449088876592)

{1449088877203 #{{:ticker :GOOG, :event-id 1} {:ticker :TSLA, :event-id 4}}, 
 1449088877601 #{{:ticker :MSFT, :event-id 3}}}
```

## License

Copyright © 2015 tolitius

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
