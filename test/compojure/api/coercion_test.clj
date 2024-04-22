(ns compojure.api.coercion-test
  (:require [compojure.api.sweet :refer :all]
            [compojure.api.test-utils :refer :all]
            [clojure.test :refer [deftest]]
            [testit.core :refer :all]
            [ring.util.http-response :refer :all]
            [clojure.core.async :as a]
            [schema.core :as s]
            [compojure.api.coercion.schema :as cs]))

(defn has-body [expected]
  (fn [value]
    (= (second value) expected)))

(defn fails-with [expected-status]
  (fn [[status body]]
    (and (= status expected-status)
         (every? (partial contains? body) [:type :coercion :in :value :schema :errors]))))

(deftest schema-coercion-test
  (fact "response schemas"
    (let [r-200 (GET "/" []
                  :query-params [{value :- s/Int nil}]
                  :responses {200 {:schema {:value s/Str}}}
                  (ok {:value (or value "123")}))
          r-default (GET "/" []
                      :query-params [{value :- s/Int nil}]
                      :responses {:default {:schema {:value s/Str}}}
                      (ok {:value (or value "123")}))
          r-200-default (GET "/" []
                          :query-params [{value :- s/Int nil}]
                          :responses {200 {:schema {:value s/Str}}
                                      :default {:schema {:value s/Int}}}
                          (ok {:value (or value "123")}))]
      (fact "200"
        (get* (api r-200) "/") => (has-body {:value "123"})
        (get* (api r-200) "/" {:value 123}) => (fails-with 500))

      (fact "exception data"
        (get* (api r-200) "/" {:value 123})
        => (contains
             [500
              {:type "compojure.api.exception/response-validation"
               :coercion "schema",
               :in ["response" "body"],
               :value {:value 123},
               :schema "{:value java.lang.String}",
               :errors {:value "(not (instance? java.lang.String 123))"}}]))

      (fact ":default"
        (get* (api r-default) "/") => (has-body {:value "123"})
        (get* (api r-default) "/" {:value 123}) => (fails-with 500))

      (fact ":default"
        (get* (api r-200-default) "/") => (has-body {:value "123"})
        (get* (api r-200-default) "/" {:value 123}) => (fails-with 500))))

  (fact "custom coercion"

    (fact "response coercion"
      (let [ping-route (GET "/ping" []
                         :return {:pong s/Str}
                         (ok {:pong 123}))]

        (fact "by default, applies response coercion"
          (let [app (api
                      ping-route)]
            (get* app "/ping") => (fails-with 500)))

        (fact "response-coercion can be disabled"
          (fact "separately"
            (let [app (api
                        {:coercion (cs/create-coercion (dissoc cs/default-options :response))}
                        ping-route)]
              (let [[status body] (get* app "/ping")]
                status => 200
                body => {:pong 123})))
          (fact "all coercion"
            (let [app (api
                        {:coercion nil}
                        ping-route)]
              (let [[status body] (get* app "/ping")]
                status => 200
                body => {:pong 123}))))

        (fact "coercion for async handlers"
          (binding [*async?* true]
            (fact "successful"
              (let [app (api (GET "/async" []
                               :return s/Str
                               (a/go (ok "abc"))))]
                (get* app "/async") => (has-body "abc")))
            (fact "failing"
              (let [app (api (GET "/async" []
                               :return s/Int
                               (a/go (ok "foo"))))]
                (get* app "/async") => (fails-with 500)))))))

    (fact "body coersion"
      (let [beer-route (POST "/beer" []
                         :body [body {:beers #{(s/enum "ipa" "apa")}}]
                         (ok body))]

        (fact "by default, applies body coercion (to set)"
          (let [app (api
                      beer-route)]
            (let [[status body] (post* app "/beer" (json-string {:beers ["ipa" "apa" "ipa"]}))]
              status => 200
              body => {:beers ["ipa" "apa"]})))

        (fact "body-coercion can be disabled"
          (let [no-body-coercion (cs/create-coercion (dissoc cs/default-options :body))
                app (api
                      {:coercion no-body-coercion}
                      beer-route)]
            (let [[status body] (post* app "/beer" (json-string {:beers ["ipa" "apa" "ipa"]}))]
              status => 200
              body => {:beers ["ipa" "apa" "ipa"]}))
          (let [app (api
                      {:coercion nil}
                      beer-route)]
            (let [[status body] (post* app "/beer" (json-string {:beers ["ipa" "apa" "ipa"]}))]
              status => 200
              body => {:beers ["ipa" "apa" "ipa"]})))

        (fact "body-coercion can be changed"
          (let [nop-body-coercion (cs/create-coercion (assoc cs/default-options :body {:default (constantly nil)}))
                app (api
                      {:coercion nop-body-coercion}
                      beer-route)]
            (post* app "/beer" (json-string {:beers ["ipa" "apa" "ipa"]})) => (fails-with 400)))))

    (fact "query coercion"
      (let [query-route (GET "/query" []
                          :query-params [i :- s/Int]
                          (ok {:i i}))]

        (fact "by default, applies query coercion (string->int)"
          (let [app (api
                      query-route)]
            (let [[status body] (get* app "/query" {:i 10})]
              status => 200
              body => {:i 10})))

        (fact "query-coercion can be disabled"
          (let [no-query-coercion (cs/create-coercion (dissoc cs/default-options :string))
                app (api
                      {:coercion no-query-coercion}
                      query-route)]
            (let [[status body] (get* app "/query" {:i 10})]
              status => 200
              body => {:i "10"})))

        (fact "query-coercion can be changed"
          (let [nop-query-coercion (cs/create-coercion (assoc cs/default-options :string {:default (constantly nil)}))
                app (api
                      {:coercion nop-query-coercion}
                      query-route)]
            (get* app "/query" {:i 10}) => (fails-with 400)))))

    (fact "route-specific coercion"
      (let [app (api
                  (GET "/default" []
                    :query-params [i :- s/Int]
                    (ok {:i i}))
                  (GET "/disabled-coercion" []
                    :coercion (cs/create-coercion (assoc cs/default-options :string {:default (constantly nil)}))
                    :query-params [i :- s/Int]
                    (ok {:i i}))
                  (GET "/no-coercion" []
                    :coercion nil
                    :query-params [i :- s/Int]
                    (ok {:i i})))]

        (fact "default coercion"
          (let [[status body] (get* app "/default" {:i 10})]
            status => 200
            body => {:i 10}))

        (fact "disabled coercion"
          (get* app "/disabled-coercion" {:i 10}) => (fails-with 400))

        (fact "exception data"
          (get* app "/disabled-coercion" {:i 10})
          => (contains
               [400
                (contains
                  {:type "compojure.api.exception/request-validation"
                   :coercion "schema",
                   :in ["request" "query-params"],
                   :value {:i "10"}
                   :schema "{Keyword Any, :i Int}",
                   :errors {:i "(not (integer? \"10\"))"}})]))

        (fact "no coercion"
          (let [[status body] (get* app "/no-coercion" {:i 10})]
            status => 200
            body => {:i "10"})))))

  (facts "apiless coercion"

    (fact "use default-coercion-matchers by default"
      (let [app (context "/api" []
                  :query-params [{y :- Long 0}]
                  (GET "/ping" []
                    :query-params [x :- Long]
                    (ok [x y])))]
        (app {:request-method :get :uri "/api/ping" :query-params {}}) => (throws)
        (app {:request-method :get :uri "/api/ping" :query-params {:x "abba"}}) => (throws)
        (app {:request-method :get :uri "/api/ping" :query-params {:x "1"}}) => (contains {:body [1 0]})
        (app {:request-method :get :uri "/api/ping" :query-params {:x "1", :y 2}}) => (contains {:body [1 2]})
        (app {:request-method :get :uri "/api/ping" :query-params {:x "1", :y "abba"}}) => (throws)))

    (fact "coercion can be overridden"
      (let [app (context "/api" []
                  :query-params [{y :- Long 0}]
                  (GET "/ping" []
                    :coercion nil
                    :query-params [x :- Long]
                    (ok [x y])))]
        (app {:request-method :get :uri "/api/ping" :query-params {}}) => (throws)
        (app {:request-method :get :uri "/api/ping" :query-params {:x "abba"}}) => (contains {:body ["abba" 0]})
        (app {:request-method :get :uri "/api/ping" :query-params {:x "1"}}) => (contains {:body ["1" 0]})
        (app {:request-method :get :uri "/api/ping" :query-params {:x "1", :y 2}}) => (contains {:body ["1" 2]})
        (app {:request-method :get :uri "/api/ping" :query-params {:x "1", :y "abba"}}) => (throws)))

    (fact "context coercion is used for subroutes"
      (let [app (context "/api" []
                  :coercion nil
                  (GET "/ping" []
                    :query-params [x :- Long]
                    (ok x)))]
        (app {:request-method :get :uri "/api/ping" :query-params {:x "abba"}}) => (contains {:body "abba"})))))
