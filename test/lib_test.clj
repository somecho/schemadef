(ns lib-test
  (:require [clojure.test :refer [testing deftest is]]
            [schemadef.lib :as lib]))

(def data [["nested-refs.json"
            {"name" "adam", "detail" {"age" 1, "dim" {"height" 179}}}]])

(deftest gen-default
  (doseq [[file expected] data]
    (testing file
      (is (-> (str "test/data/" file) slurp lib/gen-default) expected))))
