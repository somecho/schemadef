(ns schemadef.lib
  (:require [clojure.string :as str]
            [clojure.data.json :as json]))

(defn- find-refs [schema]
  (-> schema
      (get "properties")
      (->>
       (map (fn [properties]
              (if (= "object" (get (val properties) "type"))
                (find-refs (val properties))
                (contains? (val properties) "$ref")))))))

(defn- has-refs? [schema]
  (some true? (flatten (find-refs schema))))

(defn- parse-ref [ref]
  (str/split ref #"/"))

(defn- get-schema-by-ref [schema ref]
  (loop [refs (parse-ref ref)
         schema schema]
    (when (nil? schema)
      (println (str "Error: " ref " is not a valid $ref")))
    (if (empty? refs)
      schema
      (if (= "#" (first refs))
        (recur (rest refs) schema)
        (recur (rest refs) (get schema (first refs)))))))

(defn- fill-refs [schema parent]
  (if (= "object" (get schema "type"))
    (assoc schema "properties"
           (into {} (map (fn [p]
                           [(key p) (fill-refs (val p) parent)])
                         (get schema "properties"))))
    (if (contains? schema "$ref")
      (get-schema-by-ref parent (get schema "$ref"))
      schema)))

(defn- fill-refs-r [schema parent]
  (loop [schema schema]
    (if (has-refs? schema)
      (recur (fill-refs schema parent))
      schema)))

(defn gen-default
  "Generates default object from schema object.

**Inputs**
- `schema` - JSON schema as a string
 "
  [schema]
  (let [s (if (map? schema) schema (json/read-str schema))]
    (apply merge
           (map (fn [entry]
                  (if (= "object" (get (val entry) "type"))
                    {(key entry) (gen-default (val entry))}
                    {(key entry) (get (val entry) "default")}))
                (get (fill-refs-r s s) "properties")))))