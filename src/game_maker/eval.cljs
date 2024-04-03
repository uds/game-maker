(ns game-maker.eval
  (:require [cljs.js :as cljs]
            [cljs.env :as env]
            [shadow.cljs.bootstrap.browser :as boot]))
  
(def ^:private compile-state (env/default-compiler-env))

(def ^:private dsl-api-ns 'game-maker.dsl-api)
(def ^:private dsl-ns     'game-maker.dsl)

(defn evaluate
  "Used to evaluate generated DSL ClojureScript code in the context of the DSL namespace."
  [expr]
  (js/console.log "[DEBUG] Evaluating:" expr)
  (let [opts {:eval    cljs/js-eval
              :load    (partial boot/load compile-state)
              :context :statement
              :ns      dsl-api-ns}]
    (cljs/eval-str compile-state expr "[expr]" opts
                   (fn [result]
                     (if-let [error (:error result)]
                       (do (js/console.error "Evaluation error:" error)
                           (throw (js/Error. (str "Evaluation error: " (.-message error)))))
                       (js/console.log "[DEBUG] Evaluation result:" (:value result)))))))

(defn init
  "Initializes the self-hosted Clojure bootstrap environment for the DSL namespace.
   See https://code.thheller.com/blog/shadow-cljs/2017/10/14/bootstrap-support.html"
  [cb]
  (boot/init compile-state
             {:path "/js/bootstrap"
              :load-on-init [dsl-ns]}
             cb))