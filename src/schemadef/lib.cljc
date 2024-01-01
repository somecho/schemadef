(ns schemadef.lib
  (:require [schemadef.lib.core :refer [fill-refs-r]]
            #?(:clj [clojure.data.json :as json])))

(defn gen-default
  "Generates default object from schema object.

**Inputs**
- `schema` - JSON schema as a string
 "
  [schema]
  (let [s (if (map? schema)
            schema
            #?(:clj (json/read-str schema)
               :cljs (.parse js/JSON schema)))]
    (apply merge
           (map (fn [entry]
                  (if (= "object" (get (val entry) "type"))
                    {(key entry) (gen-default (val entry))}
                    {(key entry) (get (val entry) "default")}))
                (get (fill-refs-r s s) "properties")))))
