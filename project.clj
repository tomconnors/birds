(defproject birds "0.0.1"
  :description "One way to structure a clojure + clojurescript web application"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript  "0.0-2030"]
                 [org.clojure/core.async "0.1.242.0-44b1e3-alpha"]
                 [ring "1.2.1"]
                 [fogus/ring-edn "0.2.0"]
                 [clout "1.1.0"]
                 [compojure "1.1.6"]             
                 [hiccup "1.0.4"]
                 [environ "0.4.0"]
                 [com.cemerick/friend "0.2.0"]
                 [prismatic/dommy "0.1.2"]
                 [cljs-ajax "0.2.2"]]
  :plugins [[lein-cljsbuild "1.0.0-alpha2"]
            [com.keminglabs/cljx "0.3.1"]]
  :cljx {:builds [{:source-paths ["src/shared"]
                   :output-path "target/generated/clj"
                   :rules :clj}
                  {:source-paths ["src/shared"]
                   :output-path "target/generated/cljs"
                   :rules :cljs}]}
  :hooks [cljx.hooks]
  :source-paths ["src/server" "target/generated/clj"]
  :profiles {:uberjar {:aot :all}
             :dev {:jvm-opts ["-Ddev=true"]
                   :source-paths ["dev"]
                   :dependencies [[org.clojure/tools.namespace "0.2.3"]
                                  [org.clojure/java.classpath "0.2.0"]
                                  [org.clojure/tools.nrepl "0.2.3"]]
                   :plugins [[com.cemerick/austin "0.1.3"]]}}
  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src/client" "target/generated/cljs"]
                        :compiler {:output-to "resources/public/js/gen-dev/main.js" 
                                   :output-dir "resources/public/js/gen-dev/out/"
                                   :optimizations :none
                                   :libs ["resources/public/js/react"]
                                   :source-map-path "resources/public/js/gen-dev/"
                                   :source-map true}}
                       {:id "prd"
                        :source-paths ["src/client" "target/generated/cljs"]
                        :compiler {:output-to "resources/public/js/gen/main.js"
                                   :output-dir "resources/public/js/gen/"
                                   :optimizations :advanced
                                   :libs ["resources/public/js/react"]
                                   :externs ["resources/public/js/lib/history.js"]}}]})

