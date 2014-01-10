(ns blabla.core
  (:require [compojure.core :refer [GET POST defroutes]]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [clojure.java.io :refer [resource]]))

(defn main-page []
  (resource "html/page.html"))

(defroutes routes
  (GET "/" [] (main-page))
  (route/resources "/")
  (route/not-found "Gå vekk!"))