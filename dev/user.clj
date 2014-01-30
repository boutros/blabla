(ns user
  (:require [clojure.walk :refer [keywordize-keys]]
            [clojure.set :refer [rename-keys]]
            [knakk.query-bank.core :refer [query-bank]]
            [clj-http.client :as http]
            [cheshire.core :as json]
            [clojurewerkz.elastisch.rest :as esr]
            [clojurewerkz.elastisch.rest.index :as esi]
            [clojurewerkz.elastisch.rest.document :as esd]
            [clojurewerkz.elastisch.rest.bulk :as bulk]
            [clojure.tools.namespace.repl :refer [refresh-all]])
  (:import [org.jsoup Jsoup]))


(def query (query-bank "sparql/qbank.sparql"))

(def config
  {:endpoint "http://marc2rdf.deichman.no/sparql-auth"
   :http-options {:throw-exceptions true :debug false :debug-body false
                  :socket-timeout 5000 :conn-timeout 500}})

;; RESET THEESE TO REAL VALUES IN REPL
(def username "admin")
(def password "secret")

(def mapping
  (->> (slurp (clojure.java.io/resource "mappings.json"))
       json/parse-string
       keywordize-keys))

(defn setup-index! []
  (println "Creating index & mapping..")
  (esr/connect! "http://127.0.0.1:9200")
  (esi/delete "reviews")
  (esi/create "reviews"
              :settings (:settings mapping)
              :mappings (:mappings mapping)))

(defn fetch
  [q]
  (->>
    (http/get (config :endpoint)
              (merge (config :http-options)
                     {:digest-auth [username password]
                      :query-params
                      {:query q
                       :format "application/sparql-results+json"}}))
    :body
    json/parse-string
    keywordize-keys))

(defn bindings
  "Returns the bindings of a sparql/json response in a map with the binding
  parameters as keys and results as sets:

  {:binding1 #{value1, value2} :binding2 #{v1, v2}}"
  [response]
  (let [{{vars :vars} :head {solutions :bindings} :results}
        response vars (map keyword vars)]
    (into {}
          (for [v vars]
            [v (set (keep #(->> % v :value) solutions))]))))

(defn solutions
  "Returns the solution maps from a sparql/json response."
  [response]
  (for [solution (->> response :results :bindings)]
    (into {}
          (for [[k v] solution]
            [k (:value v)]))))

(defn extract
  "Extract selected variables from solutions. Return a set of maps."
  [vars solutions]
  (set (remove empty? (map #(select-keys % vars) solutions))))

(defn fetch-review [uri]
  (let [res (fetch (query "review" {:uri uri}))
        b (bindings res)
        s (solutions res)
        author (map #(rename-keys % {:creator :_id :creatorName :name})
                    (extract [:creator :creatorName] s))
        source {:_id (->> b :source first)
                :name (->> b :sourceName first)}
        reviewer {:_id (->> b :reviewer first)
                  :name (->> b :reviewerName first)
                  :source source}
        work {:_id (->> b :work first)
              :author author
              :original-title (->> b :workTitle first)
              :title (->> b :editionTitle first)
              :subtitle (->> b :editionSubTitle first)}
        text (->> b :text first)]
    {:_id uri
     :title (->> b :title first)
     :teaser (->> b :teaser first)
     :text text
     :text-stripped (when text (->> text Jsoup/parse .text))
     :issued (->> b :issued first)
     :image (->> b :image first)
     :reviewer reviewer
     :work work}))

(defn index-review!
  [r]
  (let [review (fetch-review r)]
    (esd/put "reviews" "review" (review :_id) review)
    (esd/put "reviews" "work" (->> review :work :_id) (review :work))
    (esd/put "reviews" "reviewer" (->> review :reviewer :_id) (review :reviewer))
    (doseq [author (->> review :work :author)]
      (esd/put "reviews" "author" (author :_id) author))))

(defn index-all! []
  (doseq [r (->> (query "reviews-all") fetch bindings :review)]
    (try
      (index-review! r)
      (catch Exception e (println (str r "\n" (.getMessage e)))))))

(esr/connect! "http://127.0.0.1:9200")