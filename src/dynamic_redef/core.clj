(ns dynamic-redef.core
  (:require [dynamic-redef.utils :as utils]))

(def ^:dynamic local-redefinitions {})

(defn current->original-definition
  [v]
  (when (var? v)
    (get (meta v) :original)))


(defn var->redefiniton-fn
  [a-var]
  (fn [& args]
    (let [original-f (current->original-definition a-var)
          current-f (get local-redefinitions
                         a-var
                         original-f)]
      (try
        (let [result (apply current-f args)]
          result)
        (catch Exception e
          (throw e))))))


(defn dynamic-redefs
  [vars func]
  (let [un-redefs (remove #(:already-bound? (meta %)) vars)]
    (doseq [a-var un-redefs]
      (locking a-var
        (when-not (:already-bound? (meta a-var))
          (let [old-val (.getRawRoot ^clojure.lang.Var a-var)
                metadata {:already-bound? true
                          :original old-val}]
            (.bindRoot ^clojure.lang.Var a-var
                       (with-meta (var->redefiniton-fn a-var)
                                  (merge (meta (get local-redefinitions a-var))
                                         metadata)))
            (alter-meta! a-var
                         (fn [m]
                           (merge m metadata))))))))
  (func))


(defmacro with-dynamic-redefs
  [bindings & body]
  (let [map-bindings (utils/xs->map bindings)]
    `(let [old-rebindings# local-redefinitions]
       (binding [local-redefinitions (merge old-rebindings# ~map-bindings)]
         (dynamic-redefs ~(vec (keys map-bindings))
                         (fn [] ~@body))))))
