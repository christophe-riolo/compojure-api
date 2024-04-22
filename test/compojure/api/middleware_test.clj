(ns compojure.api.middleware-test
  (:require [compojure.api.middleware :refer :all]
            [compojure.api.exception :as ex]
            [testit.core :refer :all]
            [clojure.test :refer [deftest]]
            [ring.util.http-response :refer [ok]]
            [ring.util.http-status :as status]
            [ring.util.test])
  (:import (java.io PrintStream ByteArrayOutputStream)))

(defmacro without-err
  "Evaluates exprs in a context in which *err* is bound to a fresh
  StringWriter.  Returns the string created by any nested printing
  calls."
  [& body]
  `(let [s# (PrintStream. (ByteArrayOutputStream.))
         err# (System/err)]
     (System/setErr s#)
     (try
       ~@body
       (finally
         (System/setErr err#)))))

(deftest encode?-test
  (tabular
    (fact
      (encode? nil
               {:body ?body
                :compojure.api.meta/serializable? ?serializable?}) => ?res)
    ?body ?serializable? ?res
    5 true true
    5 false false
    "foobar" true true
    "foobar" false false

    {:foobar "1"} false true
    {:foobar "1"} true true
    [1 2 3] false true
    [1 2 3] true true

    (ring.util.test/string-input-stream "foobar") false false))

(def default-options (:exceptions api-middleware-defaults))

(defn- call-async [handler request]
  (let [result (promise)]
    (handler request #(result [:ok %]) #(result [:fail %]))
    (if-let [[status value] (deref result 1500 nil)]
      (if (= status :ok)
        value
        (throw value))
      (throw (Exception. "Timeout while waiting for the request handler.")))))

(deftest wrap-exceptions-test
  (with-out-str
    (without-err
      (let [exception (RuntimeException. "kosh")
            exception-class (.getName (.getClass exception))
            handler (-> (fn [_] (throw exception))
                        (wrap-exceptions default-options))
            async-handler (-> (fn [_ _ raise] (raise exception))
                              (wrap-exceptions default-options))]

        (fact "converts exceptions into safe internal server errors"
          (handler {}) => (contains {:status status/internal-server-error
                                     :body (contains {:class exception-class
                                                      :type "unknown-exception"})})
          (call-async async-handler {}) => (contains {:status status/internal-server-error
                                                      :body (contains {:class exception-class
                                                                       :type "unknown-exception"})})))))

  (with-out-str
    (without-err
      (fact "Thrown ex-info type can be matched"
        (let [handler (-> (fn [_] (throw (ex-info "kosh" {:type ::test})))
                          (wrap-exceptions (assoc-in default-options [:handlers ::test] (fn [ex _ _] {:status 500 :body "hello"}))))]
          (handler {}) => (contains {:status status/internal-server-error
                                     :body "hello"})))))

  (without-err
    (fact "Default handler logs exceptions to console"
      (let [handler (-> (fn [_] (throw (RuntimeException. "kosh")))
                        (wrap-exceptions default-options))]
        (with-out-str (handler {})) => "ERROR kosh\n")))

  (without-err
    (fact "Default request-parsing handler does not log messages"
      (let [handler (-> (fn [_] (throw (ex-info "Error parsing request" {:type ::ex/request-parsing} (RuntimeException. "Kosh"))))
                        (wrap-exceptions default-options))]
        (with-out-str (handler {})) => "")))

  (without-err
    (fact "Logging can be added to a exception handler"
      (let [handler (-> (fn [_] (throw (ex-info "Error parsing request" {:type ::ex/request-parsing} (RuntimeException. "Kosh"))))
                        (wrap-exceptions (assoc-in default-options [:handlers ::ex/request-parsing] (ex/with-logging ex/request-parsing-handler :info))))]
        (with-out-str (handler {})) => "INFO Error parsing request\n"))))

(deftest issue-228-test ; "compose-middeleware strips nils aways. #228"
  (let [times2-mw (fn [handler]
                    (fn [request]
                      (* 2 (handler request))))]
    (((compose-middleware [nil times2-mw nil]) (constantly 3)) anything) => 6))
