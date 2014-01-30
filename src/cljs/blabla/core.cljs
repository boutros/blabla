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
         :title "Søkk borte"
         :results
         [{:took 6, :timed_out false, :_shards {:total 5, :successful 5, :failed 0}, :hits {:total 29, :max_score 6.838605, :hits [{:_index "reviews", :_type "work", :_id "http://data.deichman.no/work/x12557100_heidis_lehr-_und_wanderjahre", :_score 6.838605, :_source {:_id "http://data.deichman.no/work/x12557100_heidis_lehr-_und_wanderjahre", :author [{:name "Johanna Spyri", :_id "http://data.deichman.no/person/x12557100"}], :original-title "Heidis Lehr- und Wanderjahre", :title "Heidi", :subtitle nil}} {:_index "reviews", :_type "work", :_id "http://data.deichman.no/work/x13311000_heilt_konge", :_score 4.426565, :_source {:_id "http://data.deichman.no/work/x13311000_heilt_konge", :author [{:name "Hans Sande", :_id "http://data.deichman.no/person/x13311000"}], :original-title "Heilt konge", :title "Heilt konge", :subtitle "dikt for unge og galne"}} {:_index "reviews", :_type "work", :_id "http://data.deichman.no/work/x35088000_the_last_anniversary", :_score 4.274128, :_source {:_id "http://data.deichman.no/work/x35088000_the_last_anniversary", :author [{:name "Liane Moriarty", :_id "http://data.deichman.no/person/x35088000"}], :original-title "The last anniversary", :title "Stor ståhei", :subtitle nil}}]}} {:took 10, :timed_out false, :_shards {:total 5, :successful 5, :failed 0}, :hits {:total 30, :max_score 4.4219112, :hits [{:_index "reviews", :_type "author", :_id "http://data.deichman.no/person/x21009000", :_score 4.4219112, :_source {:name "Peter Svalheim", :_id "http://data.deichman.no/person/x21009000"}} {:_index "reviews", :_type "author", :_id "http://data.deichman.no/person/x10631900", :_score 4.4219112, :_source {:name "Helme Heine", :_id "http://data.deichman.no/person/x10631900"}} {:_index "reviews", :_type "author", :_id "http://data.deichman.no/person/x13700300", :_score 4.4219112, :_source {:name "Minken Fosheim", :_id "http://data.deichman.no/person/x13700300"}}]}} {:took 11, :timed_out false, :_shards {:total 5, :successful 5, :failed 0}, :hits {:total 33, :max_score 1.2137821, :hits [{:_index "reviews", :_type "review", :_id "http://data.deichman.no/bookreviews/onskebok/id_2155", :_score 1.2137821, :_source {:_id "http://data.deichman.no/bookreviews/onskebok/id_2155", :title "Utstøtt", :teaser nil, :text "<p>Hui og hei for en bok! Her bør du ha lest bøkene i rekkefølge, men det er det vel verdt. Sjamankunst, forbannelser, ulvespråk og menneskejakt. En dødsspennende fortelling som finner sted i steinalderen.</p>", :issued "2009-11-19T00:00:00+02:00", :image "http://www.bokkilden.no/SamboWeb/servlet/VisBildeServlet?produktId=2897427", :reviewer {:_id "http://data.deichman.no/reviewer/id_0", :name "Anonymous", :source {:_id "http://data.deichman.no/source/test", :name "Testkilde for Testing"}}, :work {:_id "http://data.deichman.no/work/x33839300_outcast", :author [{:name "Michelle Paver", :_id "http://data.deichman.no/person/x33839300"}], :original-title "Outcast", :title "Utstøtt", :subtitle nil}}, :highlight {:text ["<p>Hui og <em>hei</em> for en bok!"]}} {:_index "reviews", :_type "review", :_id "http://data.deichman.no/asker_bibliotek/review/id_400", :_score 1.0388317, :_source {:_id "http://data.deichman.no/asker_bibliotek/review/id_400", :title "Høyt over husene", :teaser "Heisekranfører Adolf har sluttet å gå hjem. Ingen savnet ham uansett. Nå bor han i heisekranen sin, høyt over husene.", :text "<p>Om kvelden spiller han på flygelhorn. Alle i byen hører det, men ingen vet hvor det kommer fra.</p><p>Passer for alderen 4+</p><p> </p><p>Det var et tusen ett hundre trappetrinn opp til kranhuset så ingen kom på besøk. Bare rengjøringshjelpen, en gang i måneden og de skravler alltid så mye. Helt til Ramona dukker opp. Adolf blir helt betatt og inviterer henne på rullekake og kaffe. Men vil hun i det hele tatt dukke opp?</p><p> </p><p>Dette er en søt liten fortelling om ensomhet, kjærlighet og håp.</p>", :issued "2013-12-18T14:32:51+01:00", :image "http://www.bokkilden.no/SamboWeb/servlet/VisBildeServlet?produktId=2813767", :reviewer {:_id "http://data.deichman.no/reviewer/id_334", :name "Stine Runshaug", :source {:_id "http://data.deichman.no/source/asker_bibliotek", :name "Asker bibliotek"}}, :work {:_id "http://data.deichman.no/work/x28371200_hoeyt_over_husene", :author [{:name "Marvin Halleraker", :_id "http://data.deichman.no/person/x28371200"}], :original-title "Høyt over husene", :title "Høyt over husene", :subtitle nil}}} {:_index "reviews", :_type "review", :_id "http://data.deichman.no/bookreviews/skoleverkstedet/id_1550", :_score 1.0304986, :_source {:_id "http://data.deichman.no/bookreviews/skoleverkstedet/id_1550", :title "Emma3 og ToreHund", :teaser nil, :text "<p>Emma logger seg inn på treffstedet \"Hei!\" nesten hver eneste dag.<br/>Brukernavn: Emma3<br/>Passord: 45icodemus.<br/>Så er hun inne.</p><p>Emma sjekker profilen til en som kaller seg ToreHund. Det er det første hun gjør hver gang hun er logget på. Det er også det siste hun gjør før hun logger ut. Hun skriver ikke noe, bare ser. Ser på gutten på bildet. Han ser merkelig ut. Stor nese, hår som står rett til værs, og øyne som er store og pene - og litt triste.</p><p>Men en dag bare gjør hun det. Hun skriver til han.<br/>\"Hei, dette er Emma3. Kul side du har ...\"</p>", :issued "2013-08-01T00:00:00+02:00", :image "http://www.bokkilden.no/SamboWeb/servlet/VisBildeServlet?produktId=2760412", :reviewer {:_id "http://data.deichman.no/reviewer/id_51", :name "Marie Johansen", :source {:_id "http://data.deichman.no/source/deichmanske_bibliotek", :name "Deichmanske bibliotek"}}, :work {:_id "http://data.deichman.no/work/x12409700_emma3_og_torehund", :author [{:name "Kjersti Scheen", :_id "http://data.deichman.no/person/x12409700"}], :original-title "Emma3 og ToreHund", :title "Emma3 og ToreHund", :subtitle nil}}, :highlight {:text ["<p>Emma logger seg inn på treffstedet \"<em>Hei</em>!\"" "<br/>\"<em>Hei</em>, dette er Emma3."]}}]}} {:took 7, :timed_out false, :_shards {:total 5, :successful 5, :failed 0}, :hits {:total 37, :max_score 4.3529506, :hits [{:_index "reviews", :_type "reviewer", :_id "http://data.deichman.no/reviewer/id_264", :_score 4.3529506, :_source {:_id "http://data.deichman.no/reviewer/id_264", :name "Hildegunn Bruheim", :source {:_id "http://data.deichman.no/source/deichmanske_bibliotek", :name "Deichmanske bibliotek"}}} {:_index "reviews", :_type "reviewer", :_id "http://data.deichman.no/reviewer/id_243", :_score 4.33852, :_source {:_id "http://data.deichman.no/reviewer/id_243", :name "Lene Heiestad", :source {:_id "http://data.deichman.no/source/bergen_folkebibliotek", :name "Bergen Offentlige Bibliotek"}}} {:_index "reviews", :_type "reviewer", :_id "http://data.deichman.no/reviewer/id_88", :_score 4.171185, :_source {:_id "http://data.deichman.no/reviewer/id_88", :name "Heidi Grytten", :source {:_id "http://data.deichman.no/source/deichmanske_bibliotek", :name "Deichmanske bibliotek"}}}]}}]

         }))

;; CHANNELS
;; new-search
;; reviews-by-work
;; reviews-by-author
;; reviews-by-reviewer
;; ? reviews-by-source


;; HELPER-FUNCTIONS


(defn highlight [s q]
  "")

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

(defn hit-by-author [data owner]
  (om/component
    (dom/ul #js {:className "authors"}
      (om/build author-link (:_source data)))))

(defn hit-by-reviewer [data owner]
  (om/component
    (dom/ul #js {:className "authors"}
      (om/build reviewer-link (:_source data)))))

(defn hit-by-review [data owner]
  (om/component
    (let [hit (:_source data)]
      (dom/tr nil
        (dom/td nil
          (dom/ul #js {:className "authors"}
           (om/build-all author-link (-> hit :work :author))))
        (dom/td #js{:className "work-title"} (-> hit :work :title))
        (dom/td #js {:className "smaller"} (or (-> data :highlight :text first)
                                               (-> data :highlight :teaser first)))))))

(defn hit-by-work [data owner]
  (om/component
    (let [hit (:_source data)
          {:keys [title original-title subtitle author]} hit]
      (dom/tr nil
        (dom/td nil
          (dom/ul #js {:className "authors"}
            (om/build-all author-link author {:key :_id})))
        (dom/td nil
          (dom/strong nil title)
          (when-not (= title original-title)
            (dom/span #js {:className "orig-title"} original-title))
          (dom/div #js {:className "smaller"} subtitle))))))

(defn change [e owner]
  (om/set-state! owner :query (.. e -target -value)))

(defn set-select [n owner]
  (om/set-state! owner :selected n))

(defn query-handler [res]
 (swap! app-state assoc :results res))

(defn search-reviews [data owner]
  (reify
    om/IInitState
    (init-state [_] {:query "hei" :selected 0})
    om/IRenderState
    (render-state [_ {:keys [query selected]}]
      (dom/div #js {:className "results-all"}
         (dom/h1 nil (:title data))
         (dom/input
           #js {:ref "search-query"
                :type "text"
                :placeholder: "Søk etter anbefalinger"
                :value query
                :onChange #(change % owner)})
         (dom/button #js {:onClick (fn [e] (POST "all"
                                                 {:params {:q query}
                                                  :handler query-handler}))}
                     "SØK")
         (let [{work-hits 0 author-hits 1 review-hits 2 reviewer-hits 3} (:results data)]
           (dom/div #js {:className "results"}
                  (dom/div #js {:className "result-group"}
                    (dom/h2 nil (str (-> work-hits :hits :total) " treff i boktittel"))
                    (when (> (-> author-hits :hits :total) 0)
                      (dom/span #js {:className "expand"} "▼"))
                    (dom/table #js {:className "hit-list"}
                      (dom/tbody nil
                        (om/build-all hit-by-work (-> work-hits :hits :hits) {:key :_id}))))
                  (dom/div #js {:className "result-group"}
                    (dom/h2 #js {:onClick #(set-select 1 owner)} (str (-> author-hits :hits :total) " treff i forfatter"))
                    (when (> (-> author-hits :hits :total) 0)
                      (dom/span #js {:className "expand"} "▼"))
                    (dom/div #js {:className (if (= selected 1) "" "hidden")}
                      (om/build-all hit-by-author (-> author-hits :hits :hits) {:key :_id})
                      (when (> (-> author-hits :hits :total) 0)
                        (dom/br nil))))
                  (dom/div #js {:className "result-group"}
                    (dom/h2 nil (str (-> review-hits :hits :total) " treff i anbefalingstekst"))
                    (when (> (-> review-hits :hits :total) 0)
                      (dom/span #js {:className "expand"} "▼"))
                    (dom/table #js {:className "hit-list"}
                      (dom/tbody nil
                        (om/build-all hit-by-review (-> review-hits :hits :hits) {:key :_id}))))
                  (dom/div #js {:className "result-group"}
                    (dom/h2 nil (str (-> reviewer-hits :hits :total) " treff i anbefaler"))
                    (when (> (-> reviewer-hits :hits :total) 0)
                      (dom/span #js {:className "expand"} "▼"))
                    (om/build-all hit-by-reviewer (-> reviewer-hits :hits :hits) {:key :_id}))
                  ))))))

(om/root
  app-state
  search-reviews
  (.getElementById js/document "app"))
