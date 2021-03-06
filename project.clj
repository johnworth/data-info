(use '[clojure.java.shell :only (sh)])
(require '[clojure.string :as string])

(defn git-ref
  []
  (or (System/getenv "GIT_COMMIT")
      (string/trim (:out (sh "git" "rev-parse" "HEAD")))
      ""))

(defproject org.cyverse/data-info "2.10.0-SNAPSHOT"
  :description "provides an HTTP API for interacting with iRODS"
  :url "https://github.com/cyverse-de/data-info"
  :license {:name "BSD"
            :url "http://iplantcollaborative.org/sites/default/files/iPLANT-LICENSE.txt"}
  :manifest {"Git-Ref" ~(git-ref)}
  :uberjar-name "data-info-standalone.jar"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.codec "0.1.0"]
                 [org.clojure/tools.nrepl "0.2.10"]
                 [cheshire "5.6.3"
                   :exclusions [[com.fasterxml.jackson.dataformat/jackson-dataformat-cbor]
                                [com.fasterxml.jackson.dataformat/jackson-dataformat-smile]
                                [com.fasterxml.jackson.core/jackson-annotations]
                                [com.fasterxml.jackson.core/jackson-databind]
                                [com.fasterxml.jackson.core/jackson-core]]]
                 [com.cemerick/url "0.1.1"]
                 [dire "0.5.3"]
                 [me.raynes/fs "1.4.6"]
                 [metosin/compojure-api "1.1.8"]
                 [org.apache.tika/tika-core "1.14"]
                 [net.sf.opencsv/opencsv "2.3"]
                 [de.ubercode.clostache/clostache "1.4.0" :exclusions [org.clojure/core.incubator]]
                 [slingshot "0.12.2"]
                 [org.cyverse/clj-icat-direct "2.8.0"
                   :exclusions [[org.slf4j/slf4j-log4j12]
                                [log4j]]]
                 [org.cyverse/clj-jargon "2.8.1"
                   :exclusions [[org.slf4j/slf4j-log4j12]
                                [log4j]]]
                 [org.cyverse/clojure-commons "2.8.1"]
                 [org.cyverse/common-cli "2.8.0"]
                 [org.cyverse/common-cfg "2.8.0"]
                 [org.cyverse/common-swagger-api "2.8.2"]
                 [org.cyverse/heuristomancer "2.8.1"]
                 [org.cyverse/kameleon "3.0.1"]
                 [org.cyverse/metadata-client "3.0.0"]
                 [org.cyverse/service-logging "2.8.0"]
                 [org.cyverse/tree-urls-client "2.8.1"]
                 [org.cyverse/event-messages "0.0.1"]
                 [com.novemberain/langohr "3.5.1"]]
  :eastwood {:exclude-namespaces [data-info.routes.schemas.exists
                                  data-info.routes.schemas.tickets
                                  data-info.routes.schemas.stats
                                  data-info.routes.schemas.sharing
                                  data-info.routes.schemas.trash
                                  :test-paths]
             :linters [:wrong-arity :wrong-ns-form :wrong-pre-post :wrong-tag :misplaced-docstrings]}
  :plugins [[test2junit "1.1.3"]
            [jonase/eastwood "0.2.3"]]
  :profiles {:dev     {:dependencies   [[ring "1.5.0"]] ;; required for lein-ring with compojure-api 1.1.8+
                       :plugins        [[lein-ring "0.9.7"]]
                       :resource-paths ["conf/test"]}
             :uberjar {:aot :all}}
  :main ^:skip-aot data-info.core
  :ring {:handler data-info.routes/app
         :init data-info.core/lein-ring-init
         :port 60000
         :auto-reload? false}
  :uberjar-exclusions [#".*[.]SF" #"LICENSE" #"NOTICE"]
  :jvm-opts ["-Dlogback.configurationFile=/etc/iplant/de/logging/data-info-logging.xml"])
