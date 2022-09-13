(ns com.example.server.pathom
  "The Pathom parser that is our backend.

   Add your resolvers and 'server-side' mutations here."
  (:require [com.wsscode.pathom3.connect.indexes :as pci]
            [com.wsscode.pathom3.connect.operation :as pco]
            [com.wsscode.pathom3.connect.runner :as pcr]
            [com.wsscode.pathom3.interface.async.eql :as p.a.eql]
            [edn-query-language.core :as eql]))

;; (pco/defresolver index-explorer
;;   "This resolver is necessary to make it possible to use 'Load index' in Fulcro Inspect - EQL"
;;   [env _]
;;   {::pco/input  #{:com.wsscode.pathom.viz.index-explorer/id}
;;    ::pco/output [:com.wsscode.pathom.viz.index-explorer/index]}
;;   {:com.wsscode.pathom.viz.index-explorer/index
;;    (-> (get env ::pc/indexes)
;;        (update ::pc/index-resolvers #(into {} (map (fn [[k v]] [k (dissoc v ::pc/resolve)])) %))
;;        (update ::pc/index-mutations #(into {} (map (fn [[k v]] [k (dissoc v ::pc/mutate)])) %)))})

(pco/defresolver i-fail
  [_ _]
  {::pco/input  []
   ::pco/output [:i-fail]}
  (throw (ex-info "Fake resolver error" {})))

;; (pc/defresolver person
;;   [_ {id :person/id}]
;;   {::pco/input  [:person/id]
;;    ::pco/output [:person/id :person/name]}
;;   {:person/id id, :person/name (str "Joe #" id)})

(pco/defmutation create-random-thing [env {:keys [tmpid] :as params}]
  ;; Fake generating a new server-side entity with
  ;; a server-decided actual ID
  ;; NOTE: To match with the Fulcro-sent mutation, we
  ;; need to explicitly name it to use the same symbol
  {::pco/op-name 'com.example.client.mutations/create-random-thing
   ;::pco/params [:tempid]
   ::pco/output [:tempids]}
  (println "SERVER: Simulate creating a new thing with real DB id 123" tmpid)
  {:tempids {tmpid 123}})

(def my-resolvers-and-mutations
  "Add any resolvers you make to this list (and reload to re-create the parser)"
  [#_index-explorer create-random-thing i-fail])

(def default-env
  (-> {:com.wsscode.pathom3.error/lenient-mode? true}
      #_(p.plugin/register pbip/mutation-resolve-params) ; needed or not?
      (pci/register my-resolvers-and-mutations)))

;; Fulcro requires that we query for ::pcr/attribute-errors if we want our code
;; to see it but Pathom complains about it (see https://github.com/wilkerlucio/pathom3/issues/156)
;; (I could also make a Pathom plugin to do this but this is easier for me)
(defn- omit-error-attribute [eql]
  (-> (eql/query->ast eql)
      (update :children (partial remove #(= {:type :prop, :key ::pcr/attribute-errors} (select-keys % [:type :key]))))
      (eql/ast->query)))

(def parse (fn parser [env eql] (p.a.eql/process (merge default-env env) (omit-error-attribute eql))))

(defn make-pathom-env 
  "Create an initial pathom `env`, lifting an query params to the top level
   for easy access by any nested resolver"
  [ring-request edn-transaction]
  (let [children (-> edn-transaction eql/query->ast :children)
        query-params (reduce
                       (fn collect-params [acc {:keys [type params]}]
                         (cond-> acc
                           (and (not= :call type) (seq params))  ; skip mutations
                           (merge params)))
                       {}
                       children)]
    {:ring/request ring-request
     :query-params query-params}))
