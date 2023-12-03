(ns dynamic-redef.core-test
  (:require [clojure.test :refer :all]
            [dynamic-redef.core :as sut]))

(defn funk
  [& args]
  {:original-args args})


(deftest redef-concurrently-test
  (dotimes [_ 1000]
    (let [[p1 p2 p3] (repeatedly 3 #(list (promise) (promise) (promise)))
          f1 (future
               @(first p1)
               (try
                 (sut/with-dynamic-redefs [funk (constantly :called-by-f1)]
                   @(second p1)
                   {:fickle-mind true
                    :id :f1
                    :result (funk 10)})
                 (finally @(last p1))))

          f2 (future
               @(first p2)
               (try
                 (sut/with-dynamic-redefs [funk (constantly :called-by-f2)]
                   @(second p2)
                   {:fickle-mind true
                    :id :f2
                    :result (funk 10)})
                 (finally @(last p2))))

          f3 (future
               @(first p3)
               (try
                 @(second p3)
                 {:fickle-mind false
                  :id :f3
                  :result (funk 10)}
                 (finally @(last p3))))]

      (is (= {:original-args [10]} (funk 10)))

      (doseq [d (shuffle (concat p1 p2 p3))]
        (deliver d ::done))

      (is (= {:original-args [10]} (funk 10)))

      (is (= {:fickle-mind true
              :id :f1
              :result :called-by-f1}
             @f1))

      (is (= {:fickle-mind true
              :id :f2
              :result :called-by-f2}
             @f2))

      (is (= {:fickle-mind false
              :id :f3
              :result {:original-args [10]}}
             @f3)))))


(deftest redef-with-metadata
  (sut/with-dynamic-redefs [funk (with-meta funk {:funky true})]
    (is (:funky (meta funk)))))
