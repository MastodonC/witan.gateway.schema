(ns witan.gateway.schema
  (:require [schema.core :as s]
            #?(:clj [schema-contrib.core :as sc])
            #?(:clj [witan.gateway.macros :refer [defversions]]))
  #?(:cljs
     (:require-macros [witan.gateway.macros :refer [defversions]])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Message

(defn compare-message-type
  [t m]
  (= t (:message/type m)))

(defn query-error?
  [m]
  (contains? m :query/error))

(defn command-error?
  [m]
  (contains? m :command/error))

(defn semver?
  [x]
  (re-find #"^\d+\.\d+\.\d+$" x))

(def Semver
  (s/pred semver?))

(def DateTime
  #? (:clj  sc/ISO-Date-Time
      :cljs s/Str))

(defversions MessageBase
  "1.0.0"
  {:message/type (s/enum :query
                         :query-response
                         :command
                         :command-receipt
                         :command-processed
                         :event)})

(defversions Query
  "1.0.0"
  (merge (get MessageBase "1.0.0")
         {:query/id s/Any
          :query/edn s/Any}))

(defversions QueryResponseBase
  "1.0.0"
  (merge (get MessageBase "1.0.0")
         {:query/id s/Any}))

(defversions QueryResponse
  "1.0.0"
  (s/conditional query-error? (merge (get QueryResponseBase "1.0.0")
                                     {:query/error s/Str})
                 :else        (merge (get QueryResponseBase "1.0.0")
                                     {:query/results [s/Any]})))

(defversions Command
  "1.0.0"
  (merge (get MessageBase "1.0.0")
         {:command/key s/Keyword
          :command/version Semver
          :command/id s/Any
          (s/optional-key :command/receipt) s/Uuid
          (s/optional-key :command/created-at) DateTime
          (s/optional-key :command/params) s/Any}))

(defversions CommandProcessed
  "1.0.0"
  (merge (get MessageBase "1.0.0")
         {:command/key s/Keyword
          :command/version Semver
          :command/id s/Any
          :command/receipt s/Uuid
          :command/created-at DateTime
          (s/optional-key :command/params) s/Any}))

(defversions CommandReceipt
  "1.0.0"
  (merge (get MessageBase "1.0.0")
         {:command/key s/Keyword
          :command/version Semver
          :command/id s/Any
          :command/receipt s/Uuid
          :command/received-at DateTime}))

(defversions Event
  "1.0.0"
  (merge (get MessageBase "1.0.0")
         {:event/key s/Keyword
          :event/version Semver
          :event/params s/Any
          ;;
          :event/id s/Uuid
          :event/created-at DateTime
          :event/origin s/Str
          :command/receipt s/Uuid}))

(defversions Message
  "1.0.0"
  (s/conditional
   (partial compare-message-type :query)             (get Query "1.0.0")
   (partial compare-message-type :query-response)    (get QueryResponse "1.0.0")
   (partial compare-message-type :command)           (get Command "1.0.0")
   (partial compare-message-type :command-processed) (get CommandProcessed "1.0.0")
   (partial compare-message-type :command-receipt)   (get CommandReceipt "1.0.0")
   (partial compare-message-type :event)             (get Event "1.0.0")))

(defn validate-message
  ([version msg]
   (s/validate Semver version)
   (-> Message
       (get version)
       (s/validate msg)))
  ([version type msg]
   (s/validate (s/eq type) (:message/type msg))
   (validate-message version msg)))

(defn check-message
  [version msg]
  (s/validate Semver version)
  (-> Message
      (get version)
      (s/check msg)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Workspace

(defversions WorkspaceMessage
  "1.0.0"
  {:workspace/name s/Str
   :workspace/id s/Uuid
   :workspace/owner-id s/Uuid
   :workspace/description s/Str
   :workspace/modified DateTime
   (s/optional-key :workspace/workflow)  [s/Any]
   (s/optional-key :workspace/catalog)   [s/Any]
   (s/optional-key :workspace/owner-name) s/Str})

(defn validate-workspace
  [version msg]
  (s/validate Semver version)
  (-> WorkspaceMessage
      (get version)
      (s/validate msg)))

(defn check-workspace
  [version msg]
  (s/validate Semver version)
  (-> WorkspaceMessage
      (get version)
      (s/check msg)))
