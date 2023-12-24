(ns build
  (:require [clojure.tools.build.api :as b]
            [clojure.java.shell :refer [sh]]))

(def project-name "schemadef")
(def lib (symbol (str "org.clojars.some/" project-name)))
(def version (slurp "VERSION"))
(def jar-file (format "target/%s-%s.jar" (name lib) version))
(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))

(defn clean [_]
  (b/delete {:path "target"}))

(defn uberjar [_]
  (clean nil)
  (b/copy-dir {:src-dirs ["src"]
               :target-dir class-dir})
  (b/compile-clj {:basis basis
                  :src-dirs ["src"]
                  :class-dir class-dir})
  (b/uber {:class-dir class-dir
           :uber-file jar-file
           :basis basis
           :main (symbol (str project-name ".core"))}))

(defn native-image [_]
  (sh  "native-image"
       "-jar" (str "target/" project-name "-" version ".jar")
       "--no-fallback"
       "--features=clj_easy.graal_build_time.InitClojureClasses"
       "--enable-https"
       (str "target/" project-name)))