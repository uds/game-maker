(ns game-maker.core
  (:require ["react-dom/client" :refer [createRoot]]
            [reagent.core :as r]
            [game-maker.dsl]
            [game-maker.eval :as eval]
            [game-maker.gpt]
            [game-maker.babylon :as b2]
            [game-maker.views.main-panel :refer [main-panel]]))

(defonce ^:private app-root (createRoot (js/document.getElementById "app")))

(defn -main []
  ;; these initializations are async
  (eval/init (fn []
               (.render app-root (r/as-element [main-panel]))
               (js/console.log "[DEBUG] Game Maker started!")

               ;; Initializing babylon.js in the next render tick, to ensure that DOM was constructed 
               ;; (babylon.js needs a canvas element to hook to)
               ;; This can also be done as full mountable React component instead, but seems like overkill in this case.
               (js/setTimeout b2/create-babylon! 0))))

(defn ^:dev/after-load hot-reload []
  (-main))

(defn ^:dev/before-load close []
  (b2/dispose-babylon!))
