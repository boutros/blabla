(ns blabla.search
  (:require [clojurewerkz.elastisch.rest :as esr]
            [clojurewerkz.elastisch.rest.document :as esd]
            [clojurewerkz.elastisch.rest.multi :as multi]))

(defn- query-work [q] {:multi_match {:query q :fields ["title^10", "original-title", "subtitle"]}})
(defn- query-author [q] {:term {:name q}})
(defn- query-review [q] {:multi_match {:query q :fields ["text^2", "teaser"]}})
(defn- query-reviewer [q] {:multi_match {:query q :fields ["reviewer.name^10", "reviewer.source.name"]}})

(defn work [q]
  "searches in book title (incl. orig title & subtitle)"
  (esd/search "reviews" "work" :query (query-work q)))

(defn author [q]
  "searches in author name"
  (esd/search "reviews" "author" :query (query-author q)))

(defn review [q]
  "searches in review title, text and teaser"
  (esd/search "reviews" "review" :query (query-review q)))

(defn reviewer [q]
  "searches for reviewer name and source name"
  (esd/search "reviews" "reviewer" :query (query-reviewer q)))

(defn all [q]
  "multi-search; return 3 results from all searches"
  (let [num-hits 3
        queries [{:index "reviews" :type "work"}
                 {:query (query-work q) :size num-hits}
                 {:index "reviews" :type "author"}
                 {:query (query-author q) :size 5}
                 {:index "reviews" :type "review"}
                 {:query (query-review q) :size num-hits
                  :highlight {:fields {:text {:fragment_size 200} :teaser {:fragment_size 100}}}}
                 {:index "reviews" :type "reviewer"}
                 {:query (query-reviewer q) :size 5}]]
    (multi/search queries)))