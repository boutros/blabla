(ns blabla.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(defn widget [data]
  (om/component
    (dom/div nil "Hei der.")))

(om/root {} widget (.getElementById js/document "app"))