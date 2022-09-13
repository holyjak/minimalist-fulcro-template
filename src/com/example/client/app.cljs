(ns com.example.client.app
  (:require
   [com.fulcrologic.fulcro.application :as app]
   [com.fulcrologic.fulcro.networking.http-remote :as http]
   [edn-query-language.core :as eql]))

(defn global-eql-transform
  [ast]
  (cond-> (app/default-global-eql-transform ast)
      ;; For *queries*, make sure that if Pathom sends errors, Fulcro does not remove it:
    (not= :call (:type ast)) ; skip mutations
    (update :children conj (eql/expr->ast :com.wsscode.pathom3.connect.runner/attribute-errors))))

(defonce app (app/fulcro-app {:remotes {:remote (http/fulcro-http-remote {})}
                              :global-eql-transform global-eql-transform
                              :remote-error?
                              (fn [result]
                                (or
                                 (app/default-remote-error? result)
                                 (:com.wsscode.pathom3.connect.runner/attribute-errors (:body result))))
                              :global-error-action
                              (fn [{{:keys [body status-code error-text]} :result :as env}]
                                (println "WARN: Remote call failed"
                                         status-code
                                         error-text
                                         (:com.wsscode.pathom3.connect.runner/attribute-errors body)))}))