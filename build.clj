(ns build
  (:require [clojure.tools.build.api :as b]
            [clojure.java.shell :refer [sh]]
            [deps-deploy.deps-deploy :as dd]))


(def project-name "schemadef")
(def lib (symbol (str "org.clojars.some/" project-name)))
(def version (str (slurp "resources/VERSION") "." (b/git-count-revs {:dir "."})))
(def jar-file (format "target/%s-%s.jar" (name lib) version))
(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))
(def url (str "https://github.com/somecho/" project-name))
(def connection (str "scm:git:" url))

(defn clean [_]
  (b/delete {:path "target"}))

(defn uberjar [_]
  (clean nil)
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  (b/compile-clj {:basis basis
                  :src-dirs ["src" "resources"]
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

(defn jar [_]
  (clean nil)
  (b/write-pom {:class-dir class-dir
                :lib lib
                :version version
                :basis (b/create-basis {:project "deps.edn"})
                :scm {:connection connection
                      :developerConnection connection
                      :url url
                      :tag (b/git-process {:git-args "rev-parse HEAD"})}
                :src-dirs ["src"]
                :resource-dirs ["resources"]
                :pom-data [[:licenses
                            [:license
                             [:name "Eclipse Public License 2.0"]
                             [:url "https://opensource.org/license/epl-2-0/"]
                             [:distribution "repo"]]]]})
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  (b/jar {:class-dir "target/classes"
          :jar-file jar-file}))

(defn deploy [_]
  (dd/deploy {:installer :remote
              :artifact jar-file
              :pom-file (b/pom-path {:lib lib :class-dir class-dir})}))