(ns multim.core
  (:import [com.google.common.collect Multimap 
                                      AbstractMapBasedMultimap$NavigableAsMap
                                      ArrayListMultimap 
                                      TreeMultimap 
                                      ImmutableSortedMap
                                      ImmutableMultimap]
           [java.util Map NavigableMap Comparator]))

(def NOOP (constantly 1))

(defn tree-multimap
  ([] 
   (TreeMultimap/create))
  ([^Comparator comp-key]
   (TreeMultimap/create comp-key NOOP))
  ([^Comparator comp-key ^Comparator comp-value]
   (TreeMultimap/create comp-key comp-value)))

(defn tree-from-mm [^Multimap mm]
  (TreeMultimap/create mm))

(defn array-list-multimap
  ([] 
   (ArrayListMultimap/create))
  ([expected-keys expected-values-per-key]
   (ArrayListMultimap/create expected-keys expected-values-per-key))
  ([^Multimap mm]
   (ArrayListMultimap/create mm)))

(derive Iterable ::iterable)
(derive java.util.Map ::map)

(defn is-map? [m]
 (or (map? m) 
     (isa? (class m) ::map)))

(defn into-multi [^Multimap mm ^Map m]
  (doseq [[k v] m]
    (if (and (isa? (class v) ::iterable)
             (not (is-map? v)))
      (.putAll mm k v)
      (.put mm k v)))
  mm)

(defn into-view [mm m]
  (.asMap (into-multi mm m)))

(defn massoc [^Multimap mm k v]
  ;; dispatch on the mm type to know which one to create
  ;; if mutable, just do put
  (into-multi (tree-from-mm mm) (hash-map k v)))

(defn mput [^Multimap mm k v]
  (.put mm k v)
  mm)

(defn mget [^Multimap mm k]
  (.get mm k))

(defprotocol Sliceable 
  (from [this k])
  (to [this k]))

(extend-type com.google.common.collect.TreeMultimap
  Sliceable 
  (from [this k] 
    (-> this (.asMap)
        (.tailMap k true))) 
  (to [this k] 
    (-> this (.asMap)
        (.headMap k true))))

(extend-type com.google.common.collect.AbstractMapBasedMultimap$NavigableAsMap
  Sliceable 
  (from [this k] 
    (-> this
        (.tailMap k true))) 
  (to [this k] 
    (-> this
        (.headMap k true))))


;; playground

;; think about it, so we can do "(subseq mmap > 2)"
(defn to-sorted [mm]
  (reify clojure.lang.Sorted
    (comparator [_] (.keyComparator mm))
    (entryKey [_ e] (.getKey e))
    (seq [_ a] ())
    (seqFrom [_ k a])))
