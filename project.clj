(defproject metosin/compojure-api "1.1.14-SNAPSHOT"
  :description "Compojure Api"
  :url "https://github.com/metosin/compojure-api"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}
  :scm {:name "git"
        :url "https://github.com/metosin/compojure-api"}
  :dependencies [[prismatic/plumbing "0.5.5"]
                 [cheshire "5.9.0"]
                 [compojure "1.6.1"]
                 [prismatic/schema "1.1.12"]
                 [org.tobereplaced/lettercase "1.0.0"]
                 [frankiesardo/linked "1.3.0"]
                 [ring-middleware-format "0.7.4"]
                 [metosin/ring-http-response "0.9.1"]
                 [metosin/ring-swagger "0.26.2"]
                 [metosin/ring-swagger-ui "2.2.10"]]
  :profiles {:uberjar {:aot :all
                       :ring {:handler examples.thingie/app}
                       :source-paths ["examples/thingie/src"]
                       :dependencies [[org.clojure/clojure "1.8.0"]
                                      [http-kit "2.3.0"]
                                      [reloaded.repl "0.2.4"]
                                      [com.stuartsierra/component "0.4.0"]]}
             :dev {:repl-options {:init-ns user}
                   :plugins [[lein-clojars "0.9.1"]
                             [lein-midje "3.2.1"]
                             [lein-ring "0.12.0"]
                             [funcool/codeina "0.5.0"]]
                   :dependencies [[org.clojure/clojure "1.8.0"]
                                  [slingshot "0.12.2"]
                                  [peridot "0.5.1"]
                                  [javax.servlet/servlet-api "2.5"]
                                  [midje "1.9.9"]
                                  [com.stuartsierra/component "0.4.0"]
                                  [reloaded.repl "0.2.4"]
                                  [http-kit "2.3.0"]
                                  [criterium "0.4.5"]]
                   :ring {:handler examples.thingie/app
                          :reload-paths ["src" "examples/thingie/src"]}
                   :source-paths ["examples/thingie/src" "examples/thingie/dev-src"]
                   :main examples.server}
             :perf {:jvm-opts ^:replace ["-server"
                                         "-Xmx4096m"
                                         "-Dclojure.compiler.direct-linking=true"]}
             :logging {:dependencies [[org.clojure/tools.logging "0.5.0"]]}
             :1.8 {:dependencies [[org.clojure/clojure "1.8.0"]]}
             :1.9 {:dependencies [[org.clojure/clojure "1.9.0"]]}
             :1.10 {:dependencies [[org.clojure/clojure "1.10.1"]]}}
  :eastwood {:namespaces [:source-paths]
             :add-linters [:unused-namespaces]}
  :codeina {:sources ["src"]
            :target "gh-pages/doc"
            :src-uri "http://github.com/metosin/compojure-api/blob/master/"
            :src-uri-prefix "#L"}
  :deploy-repositories [["snapshot" {:url "https://clojars.org/repo"
                                     :username [:gpg :env/clojars_user]
                                     :password [:gpg :env/clojars_token]
                                     :sign-releases false}]
                        ["releases" {:url "https://clojars.org/repo"
                                     :username [:gpg :env/clojars_user]
                                     :password [:gpg :env/clojars_token]
                                     :sign-releases false}]]
  :release-tasks [["clean"]
                  ["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag" "--no-sign"]
                  ["deploy" "release"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]]
  :aliases {"all" ["with-profile" "dev:dev,logging:dev,1.10"]
            "start-thingie" ["run"]
            "aot-uberjar" ["with-profile" "uberjar" "do" "clean," "ring" "uberjar"]
            "test-ancient" ["midje"]
            "perf" ["with-profile" "default,dev,perf"]
            "deploy!" ^{:doc "Recompile sources, then deploy if tests succeed."}
                      ["do" ["clean"] ["midje"] ["deploy" "clojars"]]})
