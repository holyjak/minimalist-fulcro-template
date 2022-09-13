(ns com.example.server.server
  (:require [cognitect.transit :as transit]
            [com.example.server.pathom :as pathom]
            [com.fulcrologic.fulcro.server.api-middleware :as server]
            [org.httpkit.server :refer [run-server]]
            [ring.middleware.defaults :refer [wrap-defaults]]))

(defn wrap-api [handler uri]
  (fn [request]
    (if (= uri (:uri request))
      (server/handle-api-request (:transit-params request)
        #(deref (pathom/parse (pathom/make-pathom-env request %) %)))
      (do (println "Unknown uri" (:uri request) "expected" uri)
        (handler request)))))

(defn wrap-route-to-index
  "Forward all unhandled paths to index.html, assuming it is 
   frontend routing handled by the app itself"
  [handler]
  (fn [request]
    (if (re-find #"^/(js/|images/|css/|api$|.*\w+\.\w+)" (:uri request))
      (handler request)
      (-> request (assoc :uri "/index.html") handler))))

(def handler
  (let [defaults-config {:params    {:keywordize true
                                     :multipart  true
                                     :nested     true
                                     :urlencoded true}
                         :cookies   true
                         :responses {:absolute-redirects     true
                                     :content-types          true
                                     :default-charset        "utf-8"
                                     :not-modified-responses true}
                         :session   true
                         :static    {:resources "public"}}]
    (-> (fn [req] {:status 404 :body (format "Uri %s not found here" (:uri req))})
      (wrap-api "/api")
      (server/wrap-transit-params {})
      (server/wrap-transit-response 
        ;; Needed b/c Pathom response may sometimes contain an actual Exception, e.g. from a failed mutation
        {:opts{:handlers {java.lang.Exception (transit/write-handler (constantly "err") str)}}})
      (wrap-defaults defaults-config)
      wrap-route-to-index)))

;; NOTE It is enough to reload the pathom and this ns to get any Pathom changes live
(defn start [] (run-server #'handler {:port 8008}))

(defonce stop-fn (atom (start)))
