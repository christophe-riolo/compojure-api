(ns compojure.api.main
  (:require
   [clojure.edn :as edn]
   [clojure.string :as s])
  (:gen-class))

(defn resolve-start-fn []
  (let [start (some-> "./project.clj"
                      slurp
                      edn/read-string
                      (->> (drop 1))
                      (->> (apply hash-map))
                      :start)
        names (-> start str (s/split #"/") first symbol)]
    (require names)
    (resolve start)))

(defn -main [& args]
  (apply (resolve-start-fn) args))
