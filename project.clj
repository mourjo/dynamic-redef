(defproject me.mourjo/dynamic-redef "0.1.1"
  :description "Dynamically redefine Clojure functions"
  :url "https://medium.com/helpshift-engineering/a-study-in-parallelising-tests-b5253817beae"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies []
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.10.3"]]}}
  :repl-options {:init-ns dynamic-redef.core})
