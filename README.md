# fickle

Dynamically redefine Clojure functions!

[![Clojars Project](https://img.shields.io/clojars/v/me.mourjo/fickle.svg)](https://clojars.org/me.mourjo/fickle)

The goal, simply put is to redefine functions on a per-thread basis similar to how
`binding` works, but in the context of `with-redefs`. For example, the following does not
work with `with-redefs` because it changes the root of the var being redefined by another
thread.

We create two futures, both trying to redefine the same function, and the redefinition is
not isolated to the future that is redefining it:

```clj
(defn funk
  [& args]
  :default-value)

(def f1
  (future
    (with-redefs [funk (constantly :something-new)]
      (Thread/sleep 1000)
      (funk 1))))

(def f2
  (future
    (with-redefs [funk (constantly :something-else)]
      (Thread/sleep 2000)
      (funk 1))))

[(funk 1) @f1 @f2]

;; After two seconds, we expect:
;; [:default-value :something-new :something-else]
;;
;; But we can get any thing else, like:
;; [:something-else :something-else :default-value]
;;
;; Or any of the following:
;; [:something-new :something-else :something-new]
;; [:something-new :something-new :something-new]

```

This expectation of thread-level isolation of redefinition can be achieved using
`fickle.core/with-dynamic-redefs`


## Usage

Following is an example of the usage of per-thread redefinitions.

```clj

(require '[fickle.core :as fc])

(defn funk
  [& args]
  :default-value)

(def f1
  (future
    (fc/with-dynamic-redefs [funk (constantly :something-new)]
      (Thread/sleep 1000)
      (= :something-new (funk 1)))))

(def f2
  (future
    (fc/with-dynamic-redefs [funk (constantly :something-else)]
      (Thread/sleep 2000)
      (= :something-else (funk 1)))))


;; the following should return true after 2 sec
(and @f1 @f2 (= :default-value (funk 1)))

```


## Known caveats
- This per-thread rebinding will only work with Clojure concurrency
  primitives which copy per-thread bindings to newly spawned threads,
  eg, using Clojure `future`s. But will not work for, say a
  `java.lang.Thread`.
- As of now this only supports functions being bound and not other
  vars which store values, say `(def x 19)` for example.


Pull requests welcome!


## License

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
