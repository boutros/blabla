(ns blabla.search
  (:require [clojurewerkz.elastisch.rest :as esr]
            [clojurewerkz.elastisch.rest.document :as esd]
            [clojurewerkz.elastisch.rest.multi :as multi]))

(defn- query-books [q] {:multi_match {:query q :fields ["title^10", "original-title", "subtitle", "author.name^10"]}})
;(defn- query-reviews [q] {:term {:name q}})
(defn- query-reviews [q] {:multi_match {:query q :fields ["text^10", "teaser"]}})
; (defn- query-reviewer [q] {:multi_match {:query q :fields ["reviewer.name^10", "reviewer.source.name"]}})

; (defn book [q]
;   "searches in book title (incl. orig title & subtitle)"
;   (esd/search "reviews" "work" :query (query-work q)))

; (defn author [q]
;   "searches in author name"
;   (esd/search "reviews" "author" :query (query-author q)))

; (defn review [q]
;   "searches in review title, text and teaser"
;   (esd/search "reviews" "review" :query (query-review q)))

; (defn reviewer [q]
;   "searches for reviewer name and source name"
;   (esd/search "reviews" "reviewer" :query (query-reviewer q)))

(defn all [q]
  "multi-search; return results from 2 searches"
  (let [num-hits 5
        queries [{:index "reviews" :type "work"}
                 {:query (query-books q) :size num-hits}
                 {:index "reviews" :type "review"}
                 {:query (query-reviews q) :size num-hits
                  :highlight {:fields {:text {:fragment_size 200} :teaser {:fragment_size 100}}}}]]
    (multi/search queries)))

(esr/connect! "http://127.0.0.1:9200")