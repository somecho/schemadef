(ns schemadef.core
  (:gen-class)
  (:require [clojure.data.json :as json]
            [clojure.string :as str]
            [cli-matic.core :refer [run-cmd]]))

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
      (binding [*out* *err*]
        (println (str "Error: " ref " is not a valid $ref"))))
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

(defn gen-default [schema]
  (apply merge (map (fn [entry]
                      (if (= "object" (get (val entry) "type"))
                        {(key entry) (gen-default (val entry))}
                        {(key entry) (get (val entry) "default")}))
                    (get schema "properties"))))

(defn- dispatch-gen-default [{:keys [input output schema]}]
  (cond (and input schema)
        (println "Using both input -i and schema -s flags at the same time is not allowed.")
        (and (nil? input) (nil? schema))
        (println "No input given. Please use either input -i or schema -s flag.")
        :else
        (let [s (if input
                  (-> input json/read-str)
                  (-> schema json/read-str))
              f (json/write-str (gen-default (fill-refs-r s s)))]
          (if output
            (spit output f)
            (println f)))))

(def ^:private CONFIGURATION
  {:command "schemadef"
   :description "Generate defaults from JSON schema"
   :version (slurp "VERSION")
   :opts [{:as "Path to JSON schema"
           :option "input"
           :short "i"
           :type :slurp}
          {:as "JSON schema literal"
           :option "schema"
           :short "s"
           :type :string}
          {:as "Output path. If not specified, prints to STDOUT."
           :option "output"
           :short "o"
           :type :string}]
   :runs dispatch-gen-default})

(defn -main [& args]
  (run-cmd args CONFIGURATION))