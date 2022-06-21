(ns dynamic-redef.utils)

(defn xs->map
  [xs]
  (reduce (fn [acc [k v]] (assoc acc `(var ~k) v))
          {}
          (partition 2 xs)))
