(ns immutant.init
  (:require [immutant.web :as web]
            [blabla.core :refer [routes]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [ring.middleware.edn :refer [wrap-edn-params]]))

(web/start "/" (-> routes
                   (wrap-resource "public")
                   wrap-file-info
                   wrap-edn-params))
