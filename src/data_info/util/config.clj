(ns data-info.util.config
  (:use [slingshot.slingshot :only [throw+]])
  (:require [clj-jargon.init :as init]
            [clojure-commons.config :as cc]
            [clojure-commons.error-codes :as ce]
            [clojure.tools.logging :as log]
            [common-cfg.cfg :as cfg]
            [metadata-client.core :as metadata-client]))

(def docs-uri "/docs")

(def svc-info
  {:desc     "DE service for data information logic and iRODS interactions."
   :app-name "data-info"
   :group-id "org.cyverse"
   :art-id   "data-info"
   :service  "data-info"})

(def ^:private props
  "A ref for storing the configuration properties."
  (ref nil))


(def ^:private config-valid
  "A ref for storing a configuration validity flag."
  (ref true))


(def ^:private configs
  "A ref for storing the symbols used to get configuration settings."
  (ref []))


(defn masked-config
  "Returns a masked version of the data-info config as a map."
  []
  (cc/mask-config props :filters [#"(?:irods|icat)[-.](?:user|pass)"]))


(cc/defprop-optint listen-port
  "The port that data-info listens to."
  [props config-valid configs]
  "data-info.port" 60000)


(cc/defprop-optvec perms-filter
  "The list of users who should be filtered out of permissions requests, comma-separated."
  [props config-valid configs]
  "data-info.perms-filter" ["rods", "rodsadmin"])


(cc/defprop-optstr community-data
  "The path to the root directory for community data."
  [props config-valid configs]
  "data-info.community-data" "/iplant/home/shared")


(cc/defprop-optstr copy-attribute
  "The attribute to tag files with when they're a copy of another file."
  [props config-valid configs]
  "data-info.copy-key" "ipc-de-copy-from")


(cc/defprop-optstr bad-chars
  "The characters that are considered invalid in iRODS dir- and filenames."
  [props config-valid configs]
  "data-info.bad-chars" "\u0060\u0027\u000A\u0009")


(cc/defprop-optint max-paths-in-request
  "The number of paths that are allowable in an API request."
  [props config-valid configs]
  "data-info.max-paths-in-request" 1000)


(cc/defprop-optstr anon-user
  "The name of the anonymous user."
  [props config-valid configs]
  "data-info.anon-user" "anonymous")


(cc/defprop-str anon-files-base-url
  "The base url for the anon-files server."
  [props config-valid configs]
  "data-info.anon-files-base-url")


(cc/defprop-optstr metadata-base-url
  "The base URL to use when connecting to the metadata services."
  [props config-valid configs]
  "data-info.metadata.base-url" "http://metadata:60000")

(cc/defprop-optstr tree-urls-base-url
  "The base URL of the tree-urls service"
  [props config-valid configs]
  "data-info.tree-urls.base-url" "http://tree-urls:60000")

(defn tree-urls-attr [] "ipc-tree-urls")

(cc/defprop-optstr kifshare-download-template
  "The mustache template for the kifshare URL."
  [props config-valid configs]
  "data-info.kifshare-download-template" "{{url}}/d/{{ticket-id}}/{{filename}}")

(cc/defprop-str kifshare-external-url
  "The external URL for kifshare."
  [props config-valid configs]
  "data-info.kifshare-external-url")

; iRODS connection information

(cc/defprop-optstr irods-home
  "Returns the path to the home directory in iRODS. Usually /iplant/home"
  [props config-valid configs]
  "data-info.irods.home" "/iplant/home")


(cc/defprop-optstr irods-user
  "Returns the user that data-info should connect as."
  [props config-valid configs]
  "data-info.irods.user" "rods")


(cc/defprop-optstr irods-password
  "Returns the iRODS user's password."
  [props config-valid configs]
  "data-info.irods.password" "notprod")


(cc/defprop-optstr irods-host
  "Returns the iRODS hostname/IP address."
  [props config-valid configs]
  "data-info.irods.host" "irods")


(cc/defprop-optstr irods-port
  "Returns the iRODS port."
  [props config-valid configs]
  "data-info.irods.port" "1247")


(cc/defprop-optstr irods-zone
  "Returns the iRODS zone."
  [props config-valid configs]
  "data-info.irods.zone" "iplant")


(cc/defprop-optstr irods-resc
  "Returns the iRODS resource."
  [props config-valid configs]
  "data-info.irods.resc" "")


(cc/defprop-optint irods-max-retries
  "The number of retries for failed operations."
  [props config-valid configs]
  "data-info.irods.max-retries" 10)


(cc/defprop-optint irods-retry-sleep
  "The number of milliseconds to sleep between retries."
  [props config-valid configs]
  "data-info.irods.retry-sleep" 1000)


(cc/defprop-optboolean irods-use-trash
  "Toggles whether to move deleted files to the trash first."
  [props config-valid configs]
  "data-info.irods.use-trash" true)


(cc/defprop-optvec irods-admins
  "The admin users in iRODS."
  [props config-valid configs]
  "data-info.irods.admin-users" ["rods", "rodsadmin"])

; End iRODS connection information


; ICAT connection information

(cc/defprop-optstr icat-host
  "The hostname for the server running the ICAT database."
  [props config-valid configs]
  "data-info.icat.host" "irods")


(cc/defprop-optint icat-port
  "The port that the ICAT is accepting connections on."
  [props config-valid configs]
  "data-info.icat.port" "5432")


(cc/defprop-optstr icat-user
  "The user for the ICAT database."
  [props config-valid configs]
  "data-info.icat.user" "rods")


(cc/defprop-optstr icat-password
  "The password for the ICAT database."
  [props config-valid configs]
  "data-info.icat.password" "notprod")


(cc/defprop-optstr icat-db
  "The database name for the ICAT database. Yeah, it's most likely going to be 'ICAT'."
  [props config-valid configs]
  "data-info.icat.db" "ICAT")

; End ICAT connection information.


; type detection configuration

(cc/defprop-optstr type-detect-type-attribute
  "The value that goes in the attribute column for AVUs that define a file type."
  [props config-valid configs]
  "data-info.type-detect.type-attribute" "ipc-filetype")

; End of type detection configuration


(cc/defprop-optstr amqp-uri
  "The URI to use for connections to the AMQP broker."
  [props config-valid configs]
  "data-info.amqp.uri" "amqp://guest:guest@rabbit:5672/")

(cc/defprop-optstr exchange-name
  "The name of the exchange to connect to on the AMQP host."
  [props config-valid configs]
  "data-info.amqp.exchange.name" "de")

(cc/defprop-optboolean exchange-durable?
  "Whether or not the exchange is durable."
  [props config-valid configs]
  "data-info.amqp.exchange.durable" true)

(cc/defprop-optboolean exchange-auto-delete?
  "Whether or not to automatically delete the exchange."
  [props config-valid configs]
  "data-info.amqp.exchange.auto-delete" false)

(cc/defprop-optstr queue-name
  "The name of the queue to connect to on the AMQP exchange."
  [props config-valid configs]
  "data-info.amqp.queue.name" "events.data-info.queue")

(cc/defprop-optboolean queue-durable?
  "Whether or not the queue is durable."
  [props config-valid configs]
  "data-info.amqp.queue.durable" true)

(cc/defprop-optboolean queue-auto-delete?
  "Whether or not the queue is automatically deleted."
  [props config-valid configs]
  "data-info.amqp.queue.auto-delete" false)


(defn- validate-config
  "Validates the configuration settings after they've been loaded."
  []
  (when-not (cc/validate-config configs config-valid)
    (throw+ {:error_code ce/ERR_CONFIG_INVALID})))


(defn- exception-filters
  []
  (remove nil? [(icat-password) (icat-user) (irods-password) (irods-user)]))

(def metadata-client
  (memoize #(metadata-client/new-metadata-client (metadata-base-url))))

(def anon-files-base
  (memoize
   (fn []
     (if (System/getenv "ANON_FILES_PORT")
       (cfg/env-setting "ANON_FILES_PORT")
       (anon-files-base-url)))))

(defn log-environment
  []
  (log/warn "ENV? data-info.anon-files-base-url = " (anon-files-base)))

(defn load-config-from-file
  "Loads the configuration settings from a file."
  [cfg-path]
  (cc/load-config-from-file cfg-path props)
  (cc/log-config props :filters [#"irods\.user" #"icat\.user"])
  (log-environment)
  (validate-config)
  (ce/register-filters (exception-filters)))

(def jargon-cfg
  (memoize #(init/init (irods-host)
                       (irods-port)
                       (irods-user)
                       (irods-password)
                       (irods-home)
                       (irods-zone)
                       (irods-resc)
              :max-retries (irods-max-retries)
              :retry-sleep (irods-retry-sleep)
              :use-trash   (irods-use-trash))))
