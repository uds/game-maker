(ns game-maker.core
  (:require [game-maker.dsl]
            [game-maker.eval :as eval]
            [game-maker.gpt]
            [game-maker.babylon :as b2]))

(defn -main []
  ;; TODO: these initializations are async
  (eval/init #(js/console.log "[DEBUG] ClojureScript bootstrap environment initialized!"))

  (js/console.log "[DEBUG] Game Maker started!"))

(defn ^:dev/after-load hot-reload []
  (-main)
  ;; need to start babylon after hot-reload as DOM "load" event is not triggered.
  (b2/create-babylon!))

(defn ^:dev/before-load close []
  (b2/dispose-babylon!))
