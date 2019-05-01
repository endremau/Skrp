(ns backend.routes.adaption
  (:require [backend.logging :refer [get-network-collection]]
            [backend.routes.util :refer [error-handler-rep]]
            [clojure.data.json :refer [read-str]]))

(defn command-builder
  "Builds the response map"
  [{:keys [eq-proto? protos device-id]}]
  {:name "Adaption Command"
   :type "Change Protocol"
   :device-id device-id
   :options {:protocol (if eq-proto?
                         (:current protos)
                         (:new protos))
             :keep-alive-period 10
             :max-retries 3
             :waiting-time 10}})


(defn adaption-request-handler
  "HTTP GET handler for requesting network adaptions. This endpoint
  will log the data it recieves and respond with a suitable adaption.
  Query params are collection and device-id"
  [{params :query-params :as req}]
  (println (type (params "collection")))
  (let [netcoll (read-str (str (params "collection")) :key-fn keyword)
        get-proto (fn [nc]
                    (as-> nc nc
                      (filter #(= "NetworkGraph" (:type %)) nc)
                      (first nc)
                      (:protocol nc)))]
    (if netcoll
      (let [[_ data _] (get-network-collection :latest)
            db-proto (get-proto (:collection data))
            req-proto (get-proto netcoll)]
        (command-builder
         {:eq-proto? (= req-proto db-proto)
          :protos {:current db-proto
                   :new req-proto}
          :device-id (params "device-id")}))
      (error-handler-rep 400 "Bad Request"))))
