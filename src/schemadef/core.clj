(ns schemadef.core
  (:gen-class)
  (:require [clojure.data.json :as json]
            [cli-matic.core :refer [run-cmd]]))

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
        (let [f (if input (-> input json/read-str gen-default json/write-str)
                    (-> schema json/read-str gen-default json/write-str))]
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