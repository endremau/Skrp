;;;; This file is part of SKRP.
;;;;
;;;; SKRP is free software: you can redistribute it and/or modify
;;;; it under the terms of the GNU Lesser General Public License as published
;;;; by the Free Software Foundation, either version 3 of the License, or
;;;; (at your option) any later version.
;;;;
;;;; SKRP is distributed in the hope that it will be useful,
;;;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;;;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;;;; GNU Lesser General Public License for more details.
;;;;
;;;; You should have received a copy of the GNU Lesser General Public License
;;;; along with SKRP. If not, see <https://www.gnu.org/licenses/>.

(ns backend.routes.logadaption
  "Functions for handling the /logadaption endpoint"
  (:require [backend.logging :refer [insert-syslog
                                     get-device-from-id
                                     set-device-id
                                     get-adaption-from-id]]
            [backend.routes.util :refer [error-handler-rep]]))

(defn adaption-exists?
  "Returns true if the adaption is registered, false otherwise"
  [adaption_id]
  (if-not (= nil (type (get-adaption-from-id adaption_id)))
    true
    false))

(defn device-registered?
  "Returns true if device is registered, false otherwise."
  [device_id]
  (if-not (= nil (type (get-device-from-id device_id)))
    true
    false))

(defn adaption-handler
  "POST request to log adaption"
  [{params :query-params :as req}]
  (if-not (and
           (contains? params "adaption_id")
           (contains? params "device_id")
           (contains? params "description"))
    (error-handler-rep 400 "Invalid query" req)
    (let [adaption_id (read-string (params "adaption_id"))
          device_id (read-string (params "device_id"))
          description (params "description")]
      (if-not (device-registered? (read-string (params "device_id")))
        (set-device-id device_id))
      (cond
        (not (adaption-exists? adaption_id)) (let [status 404
                                                   body {"Error" "Invalid adaption_id"}]
                                               {:status status
                                                :headers {"Content-Type" "application/json"}
                                                :body body})
        (and
         (adaption-exists? adaption_id)
         (device-registered? device_id)) (let [status 200
                                               body (insert-syslog {:device_id device_id
                                                                    :adaption_id adaption_id
                                                                    :description description})]
                                           {:status status
                                            :headers {"Content-Type" "application/json"}
                                            :body body})
        :else (error-handler-rep 400 "Database error" req)))))