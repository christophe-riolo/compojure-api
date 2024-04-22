;; NOTE: This namespace is generated by compojure.api.dev.gen
(ns compojure.api.sweet (:require compojure.api.core compojure.api.api compojure.api.routes compojure.api.resource compojure.api.swagger ring.swagger.json-schema))
(def ^{:arglists (quote ([& handlers])), :doc "Create a Ring handler by combining several handlers into one."} routes compojure.api.core/routes)
(defmacro defroutes {:doc "Define a Ring handler function from a sequence of routes.\n  The name may optionally be followed by a doc-string and metadata map."} [name & routes] (list* (quote compojure.api.core/defroutes) name routes))
(defmacro let-routes {:doc "Takes a vector of bindings and a body of routes.\n\n  Equivalent to: `(let [...] (routes ...))`"} [bindings & body] (list* (quote compojure.api.core/let-routes) bindings body))
(def ^{:arglists (quote ([& handlers])), :doc "Routes without route-documentation. Can be used to wrap routes,\n  not satisfying compojure.api.routes/Routing -protocol."} undocumented compojure.api.core/undocumented)
(defmacro middleware {:doc "Wraps routes with given middlewares using thread-first macro.\n\n  Note that middlewares will be executed even if routes in body\n  do not match the request uri. Be careful with middlewares that\n  have side-effects."} [middleware & body] (list* (quote compojure.api.core/middleware) middleware body))
(defmacro context [& args] (list* (quote compojure.api.core/context) args))
(defmacro GET [& args] (list* (quote compojure.api.core/GET) args))
(defmacro ANY [& args] (list* (quote compojure.api.core/ANY) args))
(defmacro HEAD [& args] (list* (quote compojure.api.core/HEAD) args))
(defmacro PATCH [& args] (list* (quote compojure.api.core/PATCH) args))
(defmacro DELETE [& args] (list* (quote compojure.api.core/DELETE) args))
(defmacro OPTIONS [& args] (list* (quote compojure.api.core/OPTIONS) args))
(defmacro POST [& args] (list* (quote compojure.api.core/POST) args))
(defmacro PUT [& args] (list* (quote compojure.api.core/PUT) args))
(def ^{:arglists (quote ([& body])), :doc "Returns a ring handler wrapped in compojure.api.middleware/api-middlware.\n  Creates the route-table at api creation time and injects that into the request via\n  middlewares. Api and the mounted api-middleware can be configured by optional\n  options map as the first parameter:\n\n      (api\n        {:formats [:json-kw :edn :transit-msgpack :transit-json]\n         :exceptions {:handlers {:compojure.api.exception/default my-logging-handler}}\n         :api {:invalid-routes-fn (constantly nil)}\n         :swagger {:spec \"/swagger.json\"\n                   :ui \"/api-docs\"\n                   :data {:info {:version \"1.0.0\"\n                                 :title \"My API\"\n                                 :description \"the description\"}}}}\n        (context \"/api\" []\n          ...))\n\n  ### direct api options:\n\n  - **:api**                       All api options are under `:api`.\n     - **:invalid-routes-fn**        A 2-arity function taking handler and a sequence of\n                                     invalid routes (not satisfying compojure.api.route.Routing)\n                                     setting value to nil ignores invalid routes completely.\n                                     defaults to `compojure.api.routes/log-invalid-child-routes`\n     - **:disable-api-middleware?**  boolean to disable the `api-middleware` from api.\n  - **:swagger**                   Options to configure the Swagger-routes. Defaults to nil.\n                                   See `compojure.api.swagger/swagger-routes` for details.\n\n  ### api-middleware options\n\n  Opinionated chain of middlewares for web apis. Takes optional options-map.\n\n  ### Exception handlers\n\n  An error handler is a function of exception, ex-data and request to response.\n\n  When defining these options, it is suggested to use alias for the exceptions namespace,\n  e.g. `[compojure.api.exception :as ex]`.\n\n  Default:\n\n      {::ex/request-validation  ex/request-validation-handler\n       ::ex/request-parsing     ex/request-parsing-handler\n       ::ex/response-validation ex/response-validation-handler\n       ::ex/default             ex/safe-handler}\n\n  Note: Because the handlers are merged into default handlers map, to disable default handler you\n  need to provide `nil` value as handler.\n\n  Note: To catch Schema errors use `{:schema.core/error ex/schema-error-handler}`.\n\n  ### Options\n\n  - **:exceptions**                for *compojure.api.middleware/wrap-exceptions* (nil to unmount it)\n      - **:handlers**                Map of error handlers for different exception types, type refers to `:type` key in ExceptionInfo data.\n\n  - **:format**                    for ring-middleware-format middlewares (nil to unmount it)\n      - **:formats**                 sequence of supported formats, e.g. `[:json-kw :edn]`\n      - **:params-opts**             for *ring.middleware.format-params/wrap-restful-params*,\n                                     e.g. `{:transit-json {:handlers readers}}`\n      - **:response-opts**           for *ring.middleware.format-params/wrap-restful-response*,\n                                     e.g. `{:transit-json {:handlers writers}}`\n\n  - **:ring-swagger**              options for ring-swagger's swagger-json method.\n                                   e.g. `{:ignore-missing-mappings? true}`\n\n  - **:coercion**                  A function from request->type->coercion-matcher, used\n                                   in endpoint coercion for :body, :string and :response.\n                                   Defaults to `(constantly compojure.api.middleware/default-coercion-matchers)`\n                                   Setting value to nil disables all coercion\n\n  - **:components**                Components which should be accessible to handlers using\n                                   :components restructuring. (If you are using api,\n                                   you might want to take look at using wrap-components\n                                   middleware manually.). Defaults to nil (middleware not mounted)."} api compojure.api.api/api)
(defmacro defapi {:doc "Defines an api.\n\n  API middleware options:\n\n  Opinionated chain of middlewares for web apis. Takes optional options-map.\n\n  ### Exception handlers\n\n  An error handler is a function of exception, ex-data and request to response.\n\n  When defining these options, it is suggested to use alias for the exceptions namespace,\n  e.g. `[compojure.api.exception :as ex]`.\n\n  Default:\n\n      {::ex/request-validation  ex/request-validation-handler\n       ::ex/request-parsing     ex/request-parsing-handler\n       ::ex/response-validation ex/response-validation-handler\n       ::ex/default             ex/safe-handler}\n\n  Note: Because the handlers are merged into default handlers map, to disable default handler you\n  need to provide `nil` value as handler.\n\n  Note: To catch Schema errors use `{:schema.core/error ex/schema-error-handler}`.\n\n  ### Options\n\n  - **:exceptions**                for *compojure.api.middleware/wrap-exceptions* (nil to unmount it)\n      - **:handlers**                Map of error handlers for different exception types, type refers to `:type` key in ExceptionInfo data.\n\n  - **:format**                    for ring-middleware-format middlewares (nil to unmount it)\n      - **:formats**                 sequence of supported formats, e.g. `[:json-kw :edn]`\n      - **:params-opts**             for *ring.middleware.format-params/wrap-restful-params*,\n                                     e.g. `{:transit-json {:handlers readers}}`\n      - **:response-opts**           for *ring.middleware.format-params/wrap-restful-response*,\n                                     e.g. `{:transit-json {:handlers writers}}`\n\n  - **:ring-swagger**              options for ring-swagger's swagger-json method.\n                                   e.g. `{:ignore-missing-mappings? true}`\n\n  - **:coercion**                  A function from request->type->coercion-matcher, used\n                                   in endpoint coercion for :body, :string and :response.\n                                   Defaults to `(constantly compojure.api.middleware/default-coercion-matchers)`\n                                   Setting value to nil disables all coercion\n\n  - **:components**                Components which should be accessible to handlers using\n                                   :components restructuring. (If you are using api,\n                                   you might want to take look at using wrap-components\n                                   middleware manually.). Defaults to nil (middleware not mounted)."} [name & body] (list* (quote compojure.api.api/defapi) name body))
(def ^{:arglists (quote ([info] [info options])), :doc "Creates a nested compojure-api Route from enchanced ring-swagger operations map and options.\n  By default, applies both request- and response-coercion based on those definitions.\n\n  Options:\n\n  - **:coercion**       A function from request->type->coercion-matcher, used\n                        in resource coercion for :body, :string and :response.\n                        Setting value to `(constantly nil)` disables both request- &\n                        response coercion. See tests and wiki for details.\n\n  Enchancements to ring-swagger operations map:\n\n  1) :parameters use ring request keys (query-params, path-params, ...) instead of\n  swagger-params (query, path, ...). This keeps things simple as ring keys are used in\n  the handler when destructuring the request.\n\n  2) at resource root, one can add any ring-swagger operation definitions, which will be\n  available for all operations, using the following rules:\n\n    2.1) :parameters are deep-merged into operation :parameters\n    2.2) :responses are merged into operation :responses (operation can fully override them)\n    2.3) all others (:produces, :consumes, :summary,...) are deep-merged by compojure-api\n\n  3) special key `:handler` either under operations or at top-level. Value should be a\n  ring-handler function, responsible for the actual request processing. Handler lookup\n  order is the following: operations-level, top-level.\n\n  4) request-coercion is applied once, using deep-merged parameters for a given\n  operation or resource-level if only resource-level handler is defined.\n\n  5) response-coercion is applied once, using merged responses for a given\n  operation or resource-level if only resource-level handler is defined.\n\n  Note: Swagger operations are generated only from declared operations (:get, :post, ..),\n  despite the top-level handler could process more operations.\n\n  Example:\n\n  (resource\n    {:parameters {:query-params {:x Long}}\n     :responses {500 {:schema {:reason s/Str}}}\n     :get {:parameters {:query-params {:y Long}}\n           :responses {200 {:schema {:total Long}}}\n           :handler (fn [request]\n                      (ok {:total (+ (-> request :query-params :x)\n                                     (-> request :query-params :y))}))}\n     :post {}\n     :handler (constantly\n                (internal-server-error {:reason \"not implemented\"}))})"} resource compojure.api.resource/resource)
(defmacro path-for {:doc "Extracts the lookup-table from request and finds a route by name."} [route-name & arg2] (list* (quote compojure.api.routes/path-for) route-name arg2))
(def ^{:arglists (quote ([] [options])), :doc "Returns routes for swagger-articats (ui & spec). Accepts an options map, with the\n  following options:\n  **:ui**              Path for the swagger-ui (defaults to \"/\").\n                       Setting the value to nil will cause the swagger-ui not to be mounted\n  **:spec**            Path for the swagger-spec (defaults to \"/swagger.json\")\n                       Setting the value to nil will cause the swagger-ui not to be mounted\n  **:data**            Swagger data in the Ring-Swagger format.\n  **:options**\n    **:ui**            Options to configure the ui\n    **:spec**          Options to configure the spec. Nada at the moment.\n  Example options:\n    {:ui \"/api-docs\"\n     :spec \"/swagger.json\"\n     :options {:ui {:jsonEditor true}\n               :spec {}}\n     :data {:basePath \"/app\"\n            :info {:version \"1.0.0\"\n                   :title \"Sausages\"\n                   :description \"Sausage description\"\n                   :termsOfService \"http://helloreverb.com/terms/\"\n                   :contact {:name \"My API Team\"\n                             :email \"foo@example.com\"\n                             :url \"http://www.metosin.fi\"}\n                   :license {:name: \"Eclipse Public License\"\n                             :url: \"http://www.eclipse.org/legal/epl-v10.html\"}}\n            :tags [{:name \"sausages\", :description \"Sausage api-set\"}]}}"} swagger-routes compojure.api.swagger/swagger-routes)
(def ^{:arglists (quote ([schema desc & kvs])), :doc "Attach description and possibly other meta-data to a schema."} describe ring.swagger.json-schema/describe)
