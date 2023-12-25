(ns schemadef.core
  (:gen-class)
  (:require [clojure.data.json :as json]
            [cli-matic.core :refer [run-cmd]]
            [schemadef.lib :refer [gen-default]]))

(defn- dispatch-gen-default [{:keys [input output schema]}]
  (cond (and input schema)
        (println "Using both input -i and schema -s flags at the same time is not allowed.")
        (and (nil? input) (nil? schema))
        (println "No input given. Please use either input -i or schema -s flag.")
        :else
        (let [s (if input
                  (-> input json/read-str)
                  (-> schema json/read-str))
              f (json/write-str (gen-default s))]
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