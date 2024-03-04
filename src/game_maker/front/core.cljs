(ns game-maker.front.core
  (:require ["htmx.org"]
            [babylonjs :as bb]))

(defn ^:dev/after-load init []
  (js/console.log "Game Maker started!"))