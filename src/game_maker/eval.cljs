(ns game-maker.eval
  (:require [cljs.js :as cljs]
            [cljs.env :as env]
            [shadow.cljs.bootstrap.browser :as boot]))
  
(def ^:private compile-state (env/default-compiler-env))

(defn evaluate
  "Used to evaluate generated DSL ClojureScript code in the context of the game-maker.front.dsl namespace."
  [expr]
  (js/console.log "[DEBUG] Evaluating:" expr)
  (let [opts {:eval    cljs/js-eval
              :load    (partial boot/load compile-state)
              :context :statement
              :ns      'game-maker.front.dsl-api}]
    (cljs/eval-str compile-state expr "[expr]" opts
                   (fn [result]
                     (if-let [error (:error result)]
                       (do (js/console.error "Evaluation error:" error)
                           (js/alert (str "Evaluation error: " error)))
                       (js/console.log "[DEBUG] Evaluation result:" (:value result)))))))

(defn init
  "Initializes the self-hosted Clojure bootstrap environment for the game-maker.front.dsl namespace.
   See https://code.thheller.com/blog/shadow-cljs/2017/10/14/bootstrap-support.html"
  [cb]
  (boot/init compile-state
             {:path "/js/bootstrap"
              :load-on-init ['game-maker.front.dsl]}
             cb))