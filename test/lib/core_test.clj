(ns lib.core-test
  (:require [clojure.test :refer [testing deftest is]]
            [clojure.data.json :as json]
            [schemadef.lib.core :as lib]))

(def parse-ref-data [["#/$defs/item" ["#" "$defs" "item"]]
                     ["" []]])

(deftest parse-ref
  (doseq [[input expected] parse-ref-data]
    (testing (str input "->" expected)
      (is (= expected (lib/parse-ref input))))))


(def has-refs-data [["test/data/nested-refs.json" true]])

(deftest has-refs
  (doseq [[path expected] has-refs-data]
    (testing (str path "->" expected)
      (is (= (-> path slurp json/read-str lib/has-refs?) expected)))))
