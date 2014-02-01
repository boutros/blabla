(ns blabla.core
  (:require [compojure.core :refer [GET POST defroutes]]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [clojure.java.io :refer [resource]]
            [blabla.search :as query]
            [clojurewerkz.elastisch.rest :as esr]))

(defn main-page []
  (resource "html/page.html"))

(defn edn-response [b]
  {:status 200
   :headers {"Content-Type" "application/edn; charset=utf-8"}
   :body (pr-str b)})

(esr/connect! "http://127.0.0.1:9200")

(defroutes routes
  (GET "/" [] (main-page))
  ; (POST "/work" [q] (edn-response (query/work q)))
  ; (POST "/author" [q] (edn-response (query/author q)))
  ; (POST "/review" [q] (edn-response (query/review q)))
  ; (POST "/reviewer" [q] (edn-response (query/reviewer q)))
  (POST "/all" [q work-offset review-offset] (edn-response (query/all q work-offset review-offset)))
  (route/not-found "Gå vekk!"))