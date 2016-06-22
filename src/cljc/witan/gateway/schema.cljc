(ns witan.gateway.schema
  (:require [schema.core :as s]
            #?(:clj [schema-contrib.core :as sc])
            #?(:clj [witan.gateway
                     .macros :refer [defversions]]))
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

(def MessageBase
  {:message/type (s/enum :query
                         :query-response
                         :command
                         :command-receipt
                         :event)})

(def Query
  (merge MessageBase
         {:query/id s/Any
          :query/edn s/Any}))

(def QueryResponseBase
  (merge MessageBase
         {:query/id s/Any}))

(def QueryResponse
  (s/conditional query-error? (merge QueryResponseBase
                                     {:query/error s/Str})
                 :else        (merge QueryResponseBase
                                     {:query/results [s/Any]})))

(def Command
  (merge MessageBase
         {:command/key s/Keyword
          :command/version s/Str
          :command/id s/Any
          (s/optional-key :command/params) s/Any
          (s/optional-key :command/created-at) #? (:clj  sc/ISO-Date-Time
                                                   :cljs s/Str)}))

(def CommandReceipt
  (merge MessageBase
         {:command/key s/Keyword
          :command/version s/Str
          :command/id s/Any
          :command/receipt s/Uuid}))

(def Event
  (merge MessageBase
         {:event/key s/Keyword
          :event/version s/Str
          :event/params s/Any
          :event/created-at #? (:clj  sc/ISO-Date-Time
                                :cljs s/Str)
          :command/receipt s/Uuid}))

(defversions Message
  "1.0"
  (s/conditional
   (partial compare-message-type :query)           Query
   (partial compare-message-type :query-response)  QueryResponse
   (partial compare-message-type :command)         Command
   (partial compare-message-type :command-receipt) CommandReceipt
   (partial compare-message-type :event)           Event))

(defn validate-message
  [version msg]
  (-> Message
      (get version)
      (s/validate msg)))

(defn check-message
  [version msg]
  (-> Message
      (get version)
      (s/check msg)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Workspace

(defversions Workspace
  "1.0"
  {:workspace/name s/Str
   :workspace/id s/Str
   :workspace/owner s/Str})

(defn validate-workspace
  [version msg]
  (-> Workspace
      (get version)
      (s/validate msg)))

(defn check-workspace
  [version msg]
  (-> Workspace
      (get version)
      (s/check msg)))
