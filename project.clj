(defproject blabla "0.1.0-SNAPSHOT"
  :description "Prototype på anbefalingssøk"
  :url "https://github.com/boutros/blabla"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 ;; CLJ
                 [clj-http "0.7.8"]
                 [compojure "1.1.6"]
                 [cheshire "5.3.0"]
                 [fogus/ring-edn "0.2.0"]
                 [clojurewerkz/elastisch "1.4.0"]
                 [knakk/query-bank "0.7"]
                 ;; CLJS
                 [org.clojure/clojurescript "0.0-2138"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [cljs-ajax "0.2.3"]
                 [om "0.3.1"]]
  :plugins [[lein-cljsbuild "1.0.1"]]
  :source-paths ["src/clj"]
  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [[org.clojure/tools.namespace "0.2.4"]]}}
  :resource-paths ["resources"]
  :immutant {:context-path "/blabla"}
  :cljsbuild {
              :builds [{
                        :source-paths ["src/cljs"]
                        :compiler {
                                   :output-to "resources/public/js/main.js"
                                   :output-dir "resources/public/js/out"
                                   :optimizations :none
                                   :pretty-print true
                                   :source-map true }}]})
