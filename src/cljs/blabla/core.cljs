(ns blabla.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [goog.events :as events]
            [cljs.core.async :as async :refer [>! <! put! chan]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [ajax.core :refer [GET POST]])
  (:import [goog.ui IdGenerator]
           [goog.events EventType]))

;; INIT

; (extend-type number
;   ICloneable
;   (-clone [n] (js/Number. n)))

(enable-console-print!)

;; UTILS

(defn guid []
  (.getNextUniqueId (.getInstance IdGenerator)))

;; STATE

(def PAGESIZE 5)
(def MAXPAGESVISIBLE 10)
(def MOREPAGSETHRESHOLD 7)

(def app-state
  (atom {:history []
         :basket []
         :title "Blabla™ anbefalingssøk"
         :pages-works (into [] (map (fn [n] {:n n :selected (= n 1)}) (range 1 10)))
         :pages-reviews (into [] (map (fn [n] {:n n :selected (= n 3)})  (range 1 5)))
         :results [{:took 23, :timed_out false, :_shards {:total 5, :successful 5, :failed 0}, :hits {:total 144, :max_score 6.61761, :hits [{:_index "reviews", :_type "work", :_id "http://data.deichman.no/work/x32554000_the_halloween_book", :_score 6.61761, :_source {:_id "http://data.deichman.no/work/x32554000_the_halloween_book", :author [{:name "Jane Bull", :_id "http://data.deichman.no/person/x32554000"}], :original-title "The halloween book", :title "Halloween", :subtitle nil}} {:_index "reviews", :_type "work", :_id "http://data.deichman.no/work/x4084005500_dewey", :_score 6.61761, :_source {:_id "http://data.deichman.no/work/x4084005500_dewey", :author [{:name "Bret Witter", :_id "http://data.deichman.no/person/x2042003000"} {:name "Vicki Myron", :_id "http://data.deichman.no/person/x2042002500"}], :original-title "Dewey", :title "Dewey", :subtitle nil}} {:_index "reviews", :_type "work", :_id "http://data.deichman.no/work/x2076307500_ellisif_wessel", :_score 4.141571, :_source {:_id "http://data.deichman.no/work/x2076307500_ellisif_wessel", :author [{:name "Liv Marie Austrem", :_id "http://data.deichman.no/person/x10557200"} {:name "Gro Røde", :_id "http://data.deichman.no/person/x2040442600"} {:name "Elisabeth Johansen", :_id "http://data.deichman.no/person/x25307700"}], :original-title "Ellisif Wessel", :title "Ellisif Wessel", :subtitle "du ber om mit fotografi"}} {:_index "reviews", :_type "work", :_id "http://data.deichman.no/work/x11158500_the_weekend", :_score 4.141571, :_source {:_id "http://data.deichman.no/work/x11158500_the_weekend", :author [{:name "Peter Cameron", :_id "http://data.deichman.no/person/x11158500"}], :original-title "The weekend", :title "En weekend", :subtitle nil}} {:_index "reviews", :_type "work", :_id "http://data.deichman.no/work/x10211300_henrik_wergeland", :_score 4.141571, :_source {:_id "http://data.deichman.no/work/x10211300_henrik_wergeland", :author [{:name "Yngvar Ustvedt", :_id "http://data.deichman.no/person/x10211300"}], :original-title "Henrik Wergeland", :title "Henrik Wergeland", :subtitle "en biografi"}}]}} {:took 23, :timed_out false, :_shards {:total 5, :successful 5, :failed 0}, :hits {:total 10, :max_score 0.13874474, :hits [{:_index "reviews", :_type "review", :_id "http://data.deichman.no/bookreviews/deichmanno/id_53", :_score 0.13874474, :_source {:issued "2010-06-11T00:00:00+02:00", :text-stripped nil, :text nil, :work {:_id "http://data.deichman.no/work/x13664000_the_lost_language_of_cranes", :author [{:name "David Leavitt", :_id "http://data.deichman.no/person/x13664000"}], :original-title "The lost language of cranes", :title "Kranenes tapte språk", :subtitle nil}, :image nil, :title "Kranenes tapte språk av David Leawitt", :teaser "Ein moderne klassikar innan homselitteraturen. Owen er gift med Rose, og sonen deira Philip kjem heim og fortel at han er homse. Dette er vanskeleg å takle for Rose, men Owen reagerer også sterkt på nyheita ... det har han ein heilt spesiell grunn til.", :_id "http://data.deichman.no/bookreviews/deichmanno/id_53", :reviewer {:_id "http://data.deichman.no/reviewer/id_11", :name "Kari Folvik", :source {:_id "http://data.deichman.no/source/deichmanske_bibliotek", :name "Deichmanske bibliotek"}}}, :highlight {:teaser ["Ein moderne klassikar innan homselitteraturen. <em>Owen</em> er gift med Rose, og sonen deira Philip kjem" " heim og fortel at han er homse. Dette er vanskeleg å takle for Rose, men <em>Owen</em> reagerer også sterkt på nyheita ... det har han ein heilt spesiell grunn til."]}} {:_index "reviews", :_type "review", :_id "http://data.deichman.no/asker_bibliotek/review/id_232", :_score 0.12143403, :_source {:issued "2013-12-13T14:01:50+01:00", :text-stripped "Dette er en roman, og ingen biografi, men det er allikevel ikke til å unngå at leseren tenker på den virkelige Einar og personene i livet hans. Det mest interessante er hvordan forholdet mellom ekteparet Einar og Greta beskrives, hvordan styrkeforholdet endrer seg i takt med Einars bevissthet om sin identitet og de valgene har foretar. En vakker historie som tydelig får fram personenes indre konflikter. Boken har vunnet flere priser, og det lages film av historien der Nicole Kidman spiller hovedrollen.", :text "<p>Dette er en roman, og ingen biografi, men det er allikevel ikke til å unngå at leseren tenker på den virkelige Einar og personene i livet hans.</p><p>Det mest interessante er hvordan forholdet mellom ekteparet Einar og Greta beskrives, hvordan styrkeforholdet endrer seg i takt med Einars bevissthet om sin identitet og de valgene har foretar.</p><p>En vakker historie som tydelig får fram personenes indre konflikter.</p><p>Boken har vunnet flere priser, og det lages film av historien der Nicole Kidman spiller hovedrollen.</p>", :work {:_id "http://data.deichman.no/work/x24792100_the_danish_girl", :author [{:name "David Ebershoff", :_id "http://data.deichman.no/person/x24792100"}], :original-title "The Danish girl", :title "Den danske piken", :subtitle nil}, :image "http://krydder.bib.no/0781/9557514.bilde.1345618413.s.jpg", :title "Den danske piken", :teaser "I 1930 var den danske kunstneren Einar Wegener den første som gjennomgikk kjønnsskifteoperasjon. Han døde som Lili Elbe i 1931 etter den operasjonen som skulle gjøre ham i stand til å kunne bli gravid og bære fram et barn.", :_id "http://data.deichman.no/asker_bibliotek/review/id_232", :reviewer {:_id "http://data.deichman.no/reviewer/id_318", :name "Gro B. Soydan", :source {:_id "http://data.deichman.no/source/asker_bibliotek", :name "Asker bibliotek"}}}, :highlight {:teaser ["I 1930 var den danske kunstneren Einar <em>Wegener</em> den første som gjennomgikk kjønnsskifteoperasjon"]}} {:_index "reviews", :_type "review", :_id "http://data.deichman.no/bookreviews/deichmanno/id_62", :_score 0.11106123, :_source {:issued "2006-05-16T00:00:00+02:00", :text-stripped "Vi blir presentert for Dr. Wangel og hans familie. Han er gift for andre gang, med Ellida, som er mye yngre enn ham. Til familien hører også døtrene Bolette og Hilde, som er fra Wangels første ekteskap. Ellida har vokst opp ytterst i fjorden og er datter av fyrvokteren. Hun har tidligere hatt et forhold til en sjømann som hun har inngått en troskapsed med. Dette forholdet skaper uro i Ellidas tilværelse og hun har en sterk skyldfølelse fordi hun har inngått ekteskap med Wangel. Denne skyldfølelsen får etterhvert en stadig større plass i hennes tilværelse og etterhvert også i Wangels liv. Klimaks nåes når «den fremmede» kommer innom og blir gjenkjent som sjømannen fra fortiden. Han gir Ellida det ultimatum at enten følger hun med ham eller så er det forbi for godt. Hun får 24 timers betenkningstid. Temaet som går igjen i dette stykket er nettopp det å gjøre selvstendige valg, eller som Wangel uttrykker det overfor sin kone Ellida «for nu kan du velge i frihet.Kvinners mulighet til å gjøre selvstendige valg på denne tiden var svært begrenset, på samme måte som enhver form for «karriere» også var nærmest ikke-eksisterende. Det er derfor tematikken rundt valg i frihet og under ansvar får så stor betydning. Wangel bruker selv begrepet «la handelen gå om igjen» for på denne måten kan Ellida fristilles og gjøre sine valg uten å føle seg bundet, hverken til Wangel eller til det tidligere inngåtte troskapsløftet. Stykket har en svært fortettet stemning og man kan ikke unngå å bli engasjert i Ellida og hennes skjebne.", :text "<p>Vi blir presentert for Dr. Wangel og hans familie. Han er gift for andre gang, med Ellida, som er mye yngre enn ham. Til familien hører også døtrene Bolette og Hilde, som er fra Wangels første ekteskap.</p><p>Ellida har vokst opp ytterst i fjorden og er datter av fyrvokteren. Hun har tidligere hatt et forhold til en sjømann som hun har inngått en troskapsed med. Dette forholdet skaper uro i Ellidas tilværelse og hun har en sterk skyldfølelse fordi hun har inngått ekteskap med Wangel. Denne skyldfølelsen får etterhvert en stadig større plass i hennes tilværelse og etterhvert også i Wangels liv. Klimaks nåes når «den fremmede» kommer innom og blir gjenkjent som sjømannen fra fortiden. Han gir Ellida det ultimatum at enten følger hun med ham eller så er det forbi for godt. Hun får 24 timers betenkningstid.</p><p>Temaet som går igjen i dette stykket er nettopp det å gjøre selvstendige valg, eller som Wangel uttrykker det overfor sin kone Ellida «for nu kan du velge i frihet.Kvinners mulighet til å gjøre selvstendige valg på denne tiden var svært begrenset, på samme måte som enhver form for «karriere» også var nærmest ikke-eksisterende.</p><p>Det er derfor tematikken rundt valg i frihet og under ansvar får så stor betydning. Wangel bruker selv begrepet «la handelen gå om igjen» for på denne måten kan Ellida fristilles og gjøre sine valg uten å føle seg bundet, hverken til Wangel eller til det tidligere inngåtte troskapsløftet.</p><p>Stykket har en svært fortettet stemning og man kan ikke unngå å bli engasjert i Ellida og hennes skjebne.</p>", :work {:_id "http://data.deichman.no/work/x14668800_fruen_fra_havet", :author [{:name "Henrik Ibsen", :_id "http://data.deichman.no/person/x14668800"}], :original-title "Et dukkehjem", :title "Fruen fra havet", :subtitle nil}, :image "http://www.bokkilden.no/SamboWeb/servlet/VisBildeServlet?produktId=144396", :title "Fruen fra Havet (1888) av Henrik Ibsen", :teaser "Fruen fra havet ble utgitt 28. november 1888 og hadde urpremiere 12. februar 1889 både på Christiania Theater og Hoffteatret i Weimar. Handlingen foregår i en liten kystby, om sommeren.", :_id "http://data.deichman.no/bookreviews/deichmanno/id_62", :reviewer {:_id "http://data.deichman.no/reviewer/id_34", :name "Astrid Werner", :source {:_id "http://data.deichman.no/source/deichmanske_bibliotek", :name "Deichmanske bibliotek"}}}, :highlight {:teaser [" Christiania Theater og Hoffteatret i <em>Weimar</em>. Handlingen foregår i en liten kystby, om sommeren."]}} {:_index "reviews", :_type "review", :_id "http://data.deichman.no/asker_bibliotek/review/id_235", :_score 0.10517916, :_source {:issued "2013-12-13T14:04:23+01:00", :text-stripped "Det unge ekteparet Ellisif og Andreas Wessel reiste til Kirkenes. Han hadde fått stilling som distriktslege, og hun så for seg en fremtid som mor for en stor barneflokk. Slik skulle det ikke gå. Hun fulgte med mannen på sykebesøk, og hennes sosiale engasjement våknet under nødsårene rundt 1900. Med kamera og ord dokumenterte hun fattigdommen. Ellisif skrev dikt og artikler i aviser og tidsskrifter, og ekteparet lot russiske flyktninger bo hos seg.   Hennes steile holdning resulterte i at hun ble kalt inn til avhør i Kristiania for å bli vurdert om hun måtte erklæres for sinnssyk. Hun var prinsippfast til det siste, og hun nektet å møte Kong Haakon VII da han besøkte Finnmark etter krigen.", :text "<p>Det unge ekteparet Ellisif og Andreas Wessel reiste til Kirkenes. Han hadde fått stilling som distriktslege, og hun så for seg en fremtid som mor for en stor barneflokk. Slik skulle det ikke gå. Hun fulgte med mannen på sykebesøk, og hennes sosiale engasjement våknet under nødsårene rundt 1900. Med kamera og ord dokumenterte hun fattigdommen. Ellisif skrev dikt og artikler i aviser og tidsskrifter, og ekteparet lot russiske flyktninger bo hos seg.</p><p> </p><p>Hennes steile holdning resulterte i at hun ble kalt inn til avhør i Kristiania for å bli vurdert om hun måtte erklæres for sinnssyk. Hun var prinsippfast til det siste, og hun nektet å møte Kong Haakon VII da han besøkte Finnmark etter krigen.</p>", :work {:_id "http://data.deichman.no/work/x30017900_himmelstormeren", :author [{:name "Cecilie Enger", :_id "http://data.deichman.no/person/x30017900"}], :original-title "Himmelstormeren", :title "Himmelstormeren", :subtitle "en roman om Ellisif Wessel"}, :image "http://krydder.bib.no/0023/953256.bilde.1327491199.s.jpg", :title "Himmelstormeren", :teaser "Legefruen Ellisif Wessel hadde alle muligheter til å leve et overklasseliv. I stedet ble hun en glødende revolusjonær og gudløs aksjonist.", :_id "http://data.deichman.no/asker_bibliotek/review/id_235", :reviewer {:_id "http://data.deichman.no/reviewer/id_318", :name "Gro B. Soydan", :source {:_id "http://data.deichman.no/source/asker_bibliotek", :name "Asker bibliotek"}}}, :highlight {:teaser ["Legefruen Ellisif <em>Wessel</em> hadde alle muligheter til å leve et overklasseliv. I stedet ble hun en glødende revolusjonær og gudløs aksjonist."]}} {:_index "reviews", :_type "review", :_id "http://data.deichman.no/bergen_folkebibliotek/review/id_318", :_score 0.10517916, :_source {:issued "2010-01-08T16:53:02+02:00", :text-stripped "To engelske søstre reiser fra tåken i England for å sommerjobbe i Liguria midt på 80-tallet og reiser aldri mer derfra annet enn for kortere opphold i England for å rette opp finansene når inntektene fra olivenproduksjonen ikke strekker til. Søstrenes utvikling fra ”stranieris”(fremmede/utlendinger) til godkjente og innforståtte medlemmer av landsbyen Diano San Pietro er lang og til dels tornefull. De kulturelle og sosiale kodene må læres, og de er like merkelige for begge parter og fører til utallige misforståelser og frustrasjoner på begge sider. Olivendyrkingen er for eksempel et ugjennomtrengelig mysterium de første årene, men det viser seg at det meste som til å begynne med virker underlig, er resultatet av generasjoners prøving og feiling og rett og slett sunn fornuft. Boken er humoristisk britisk i tonen, aldri plagsomt privat og aldri nedlatende. Biblioteket har den også som lydbok på engelsk.  ", :text " <p> To engelske søstre reiser fra tåken i England for å sommerjobbe i Liguria midt på 80-tallet og reiser aldri mer derfra annet enn for kortere opphold i England for å rette opp finansene når inntektene fra olivenproduksjonen ikke strekker til.</p> <p>Søstrenes utvikling fra ”stranieris”(fremmede/utlendinger) til godkjente og innforståtte medlemmer av landsbyen Diano San Pietro er lang og til dels tornefull. De kulturelle og sosiale kodene må læres, og de er like merkelige for begge parter og fører til utallige misforståelser og frustrasjoner på begge sider. Olivendyrkingen er for eksempel et ugjennomtrengelig mysterium de første årene, men det viser seg at det meste som til å begynne med virker underlig, er resultatet av generasjoners prøving og feiling og rett og slett sunn fornuft.</p> Boken er humoristisk britisk i tonen, aldri plagsomt privat og aldri nedlatende. Biblioteket har den også som lydbok på engelsk. <p> </p> ", :work {:_id "http://data.deichman.no/work/x31240800_extra_virgin", :author [{:name "Annie Hawes", :_id "http://data.deichman.no/person/x31240800"}], :original-title "Extra virgin", :title "Extra virgin", :subtitle nil}, :image "http://krydder.bib.no/0226/754444.bilde.1327482044.s.jpg", :title "Annie Hawes: Extra virgin. Livet i Liguria", :teaser "Hvis Peter Mayles fortelling om sitt år i Provence fikk skarer av turister til å legge ferien dit, bør Anne Hawes bok få flokkene over til Italia.", :_id "http://data.deichman.no/bergen_folkebibliotek/review/id_318", :reviewer {:_id "http://data.deichman.no/reviewer/id_220", :name "Elin Huseby", :source {:_id "http://data.deichman.no/source/bergen_folkebibliotek", :name "Bergen Offentlige Bibliotek"}}}, :highlight {:teaser ["Hvis Peter Mayles fortelling om sitt år i Provence fikk skarer av turister til å legge ferien dit, bør Anne <em>Hawes</em> bok få flokkene over til Italia."]}}]}}]
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

(defn change [e owner]
  (om/set-state! owner :query (.. e -target -value)))

(defn query-handler [res]
 (swap! app-state assoc :results res))

;; COMPONENTS

; (defn reviewer-link [data owner]
;   (om/component
;     (dom/li nil
;       (dom/span #js {:className "linki" :onClick (fn [e] (println (@data :_id)))} (data :name))
;       (dom/br nil)
;       (dom/span #js {:className "smaller grey" :onClick (fn [e] (println (-> @data :source :_id)))} (-> data :source :name)))))

(defn person [p owner]
  (om/component
    (dom/span #js {:className "author linki"
                   :onClick (fn [e] (println (@p :_id)))}
              (p :name))))

(defn review-hit [data owner]
  (om/component
    (let [hit (:_source data)
          authors (-> hit :work :author)]
      (dom/tr nil
        (dom/td nil
          (when authors
            (om/build-all person authors {:key :_id}))
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

(defn work-hit [data owner]
  (om/component
    (let [hit (:_source data)
          {:keys [title original-title subtitle author]} hit]
      (dom/tr nil
        (dom/td nil
          (when author
            (om/build-all person author {:key :_id}))
          (dom/span #js {:className "pointer" :onClick (fn [e] (println (:_id @data)))}
            (dom/strong nil title)
            (when subtitle (dom/span #js {:className "subtitle"} subtitle))
            (when-not (= title original-title)
              (dom/span #js {:className "orig-title"} original-title))))))))



; (defn input-keyup [e owner]
;   (if (= 27 (.-keyCode e))
;     (om/set-state! owner :query "")
;     ))

(defn pagination [n owner]
  (om/component
    (dom/span #js {:className (if (:selected n) "page-selected" "page-num")
                   :onClick (fn [e] (when-not (:selected @n)
                                      (println "new page")))} (:n n))))

(defn do-query [q work-page review-page]
  (POST "all"
        {:params {:q q
                  :work-offset (quot work-page PAGESIZE)
                  :review-offset (quot review-page PAGESIZE)}
         :handler query-handler}))

(defn search-reviews [data owner]
  (reify
    om/IInitState
    (init-state [_] {:query "" :query-chan (chan)})
    om/IWillMount
    (will-mount [_]
      (let [c (om/get-state owner :query-chan)]
        (go (loop []
          (let [q (<! c)]
            (do-query (:q q) (:work-page q) (:review-page q)))
            (recur)))))
    om/IWillUpdate
    (will-update [_ next-props next-state]
      (println next-state))
    om/IRenderState
    (render-state [_ {:keys [query query-chan]}]
      (dom/div #js {:className "results-all"}
         (dom/span #js {:className "heading"} (:title data))
         (dom/input
           #js {:ref "search-query"
                :type "text"
                :className "search"
                :placeholder "Søk etter anbefalinger"
                :value query
                :onChange #(change % owner)
                :onKeyUp (throttle
                           (fn [e]
                             (put! query-chan {:q query :work-page 1 :review-page 1}))
                           300)})
         (let [{book-hits 0 review-hits 1} (:results data)
               book-hits-total (-> book-hits :hits :total)
               review-hits-total (-> review-hits :hits :total)]
           (dom/div #js {:className "results"}
              (dom/div #js {:className "result-group"}
                (dom/h2 nil (str book-hits-total " treff i forfatter / tittel"))
                (dom/table #js {:className "hit-list"}
                  (dom/tbody nil
                    (om/build-all work-hit (-> book-hits :hits :hits) {:key :_id})))
                (when (> book-hits-total PAGESIZE)
                  (dom/div #js {:className "pagination"}
                           (om/build-all pagination (:pages-works data) {:key :n})
                           (dom/span #js {:className "page-num"} ">>")))
              (dom/div #js {:className "result-group"}
                (dom/h2 nil (str review-hits-total " treff i anbefalingstekst"))
                (dom/table #js {:className "hit-list"}
                  (dom/tbody nil
                    (om/build-all review-hit (-> review-hits :hits :hits) {:key :_id})))
                (when (> review-hits-total PAGESIZE)
                  (dom/div #js {:className "pagination"}
                           (dom/span #js {:className "page-num"} "<<")
                           (om/build-all pagination (:pages-reviews data) {:key :n})
                           (dom/span #js {:className "page-num"} ">>"))))
                  )))))))

(om/root
  app-state
  search-reviews
  (.getElementById js/document "app"))
