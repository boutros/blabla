(ns blabla.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [ajax.core :refer [GET POST]])
  (:import [goog.ui IdGenerator]))

(enable-console-print!)

;; UTILS

(defn guid []
  (.getNextUniqueId (.getInstance IdGenerator)))

;; STATE

(def app-state
  (atom {:history []
         :basket []
         :title "Blabla™ anbefalingssøk"
         :results [{}{}]
         }))

;; CHANNELS
;; new-search
;; reviews-by-work
;; reviews-by-author
;; reviews-by-reviewer
;; ? reviews-by-source


;; HELPER-FUNCTIONS

(defn debounce
  ([f] (debounce f 1000))
  ([f timeout]
    (let [id (atom nil)]
      (fn [evt]
        (if (not (nil? @id))
          (js/clearTimeout @id))
        (reset! id (js/setTimeout
                   (partial f evt)
                   timeout))))))

(defn throttle [func wait]
  (let [result (atom nil)
        throttling (atom nil)
        more (atom nil)
        timeout (atom nil)
        when-done (debounce (fn []
                              (reset! more false)
                              (reset! throttling false)) wait)]
    (fn []
      (this-as this
               (let [context this
                     args js/arguments
                     later (fn []
                             (reset! timeout nil)
                             (when @more (.apply func context args))
                             (when-done))]
                 (when-not @timeout
                   (reset! timeout (js/setTimeout later wait)))
                 (if @throttling
                   (reset! more true)
                   (reset! result (.apply func context args)))
                 (when-done)
                 (reset! throttling true)
                 @result)))))

;; COMPONENTS

(defn author-link [data owner]
  (om/component
    (dom/li nil
      (dom/span #js {:className "linki" :onClick (fn [e] (println (@data :_id)))} (data :name)))))

(defn reviewer-link [data owner]
  (om/component
    (dom/li nil
      (dom/span #js {:className "linki" :onClick (fn [e] (println (@data :_id)))} (data :name))
      (dom/br nil)
      (dom/span #js {:className "smaller grey" :onClick (fn [e] (println (-> @data :source :_id)))} (-> data :source :name)))))

(defn authors [data owner]
  (om/component
    (dom/span #js {:className "author linki" :onClick (fn [e] (println (@data :_id)))} (data :name))))

(defn hit-by-review [data owner]
  (om/component
    (let [hit (:_source data)
          author (-> hit :work :author)]
      (dom/tr nil
        (dom/td nil
          (when author
            (om/build-all authors author {:key :_id}))
          (dom/strong nil (-> hit :work :title))
          (dom/div #js {:className "smaller"} "anbefalt av "
            (dom/span #js {:className "linko"}
                      (str  (-> hit :reviewer :name)
                           " / "
                           (-> hit :reviewer :source :name)))))
        (dom/td #js {:className "smaller pointer"
                     :onClick (fn [e] (println [(:_id @data) (-> @hit :work :_id)]))
                     :dangerouslySetInnerHTML #js{:__html (or (-> data :highlight :text-stripped first)
                                                              (-> data :highlight :teaser first))}})))))

(defn hit-by-work [data owner]
  (om/component
    (let [hit (:_source data)
          {:keys [title original-title subtitle author]} hit]
      (dom/tr nil
        (dom/td nil
          (when author
            (om/build-all authors author {:key :_id}))
          (dom/span #js {:className "pointer" :onClick (fn [e] (println (:_id @data)))}
            (dom/strong nil title)
            (when subtitle (dom/span #js {:className "subtitle"} subtitle))
            (when-not (= title original-title)
              (dom/span #js {:className "orig-title"} original-title))))))))

(defn change [e owner]
  (om/set-state! owner :query (.. e -target -value)))


(defn query-handler [res]
  ; (do
  ;   (println res)
 (swap! app-state assoc :results res))
  ; )

; (defn input-keyup [e owner]
;   (if (= 27 (.-keyCode e))
;     (om/set-state! owner :query "")
;     ))

(defn search-reviews [data owner]
  (reify
    om/IInitState
    (init-state [_] {:query ""})
    om/IRenderState
    (render-state [_ {:keys [query]}]
      (dom/div #js {:className "results-all"}
         (dom/span #js {:className "heading"} (:title data))
         (dom/input
           #js {:ref "search-query"
                :type "text"
                :className "search"
                :placeholder "Søk etter anbefalinger"
                :value query
                :onChange #(change % owner)
                :onKeyUp (throttle (fn [e] (POST "all"
                            {:params {:q (om/get-state owner :query)}
                             :handler query-handler})) 300)})
         (let [{book-hits 0 review-hits 1} (:results data)]
           (dom/div #js {:className "results"}
                  (dom/div #js {:className "result-group"}
                    (dom/h2 nil (str (-> book-hits :hits :total) " treff i forfatter / tittel"))
                    (dom/table #js {:className "hit-list"}
                      (dom/tbody nil
                        (om/build-all hit-by-work (-> book-hits :hits :hits) {:key :_id}))))
                  (dom/div #js {:className "result-group"}
                    (dom/h2 nil (str (-> review-hits :hits :total) " treff i anbefalingstekst"))
                    (dom/table #js {:className "hit-list"}
                      (dom/tbody nil
                        (om/build-all hit-by-review (-> review-hits :hits :hits) {:key :_id}))))
                  ))))))

(om/root
  app-state
  search-reviews
  (.getElementById js/document "app"))
