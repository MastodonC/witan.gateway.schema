(ns witan.gateway.macros)

(defmacro defversions
  [name & body]
  (let [split (partition 2 body)
        as-hm (into {} (map (fn [[k v]] (hash-map k v)) split))]
    `(def ~name ~as-hm)))
