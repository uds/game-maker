(ns game-maker.front.dsl-api
  (:require [babylonjs :as bb]))


;; ------------------------------------------------------------------------------------------------------------
;; For all functions in this API:
;;
;;  - All valid DSL functions are defined in the DslApi ClojureScript protocol. 
;;  - Every DSL function is always called with the first parameter being a reference to the context object @!dsl.
;;  - The DSL function is an invocation of the protocol method on the context object, e.g.: (method-name @!dsl ...).
;;  - The object name is a keyword.
;;  - The object position is a vector [x y z].
;;  - The object size is a vector [height width depth].
;;  - The object color is a keyword. Supported color values are listed in the "colors" map.
;; ------------------------------------------------------------------------------------------------------------


;; A global instance of the DSL API for the game maker
(def !dsl (atom nil))

;; Supported colors
(def colors {:red     (bb/Color3. 1 0 0)
             :green   (bb/Color3. 0 1 0)
             :blue    (bb/Color3. 0 0 1)
             :yellow  (bb/Color3. 1 1 0)
             :magenta (bb/Color3. 1 0 1)
             :cyan    (bb/Color3. 0 1 1)
             :white   (bb/Color3. 1 1 1)
             :black   (bb/Color3. 0 0 0)
             :gray    (bb/Color3. 0.5 0.5 0.5)
             :purple  (bb/Color3. 0.5 0 0.5)
             :orange  (bb/Color3. 1 0.5 0)})


(defn ^:export rand-range
  "Returns a random floating number between the low and high (exclusive) numbers."
  [low high]
  (+ low (rand (- high low))))


(defprotocol ^:export DslApi
  "DSL protocol for the game maker"

  ;; Disposes object with a given name.
  ;; Usage example:  (dispose @!dsl :yellow-ball)
  (dispose [this name])

  ;; Disposes all objects created so far.
  ;; Usage example:  (dispose-all @!dsl)
  (dispose-all [this])
             
  ;; Set's the world gravity to a given vector.
  ;; Usage example: (set-gravity @!dsl [0 -9.81 0])             
  (set-gravity [this gravity-vector])

  ;; Create a sphere with a given name at the given position with the given color.
  ;; Usage example:  (create-sphere @!dsl :yellow-ball [-5 0 0] 2 :yellow)
  (create-sphere [this name position diameter color])

  ;; Create a cuboid with a given name at the given position with the given dimensions and color.
  ;; Usage example:  (create-cuboid @!dsl :yellow-brick [-5 0 0] [1 2 2] :yellow)
  (create-cuboid [this name position size color])

  ;; Returns position of the object with a given name as a vector [x y z].
  ;; Usage example:  (get-position @!dsl :yellow-ball) ; => [-5 0 0]
  (get-position [this name])

  ;; Set the position of the object with a given name to the given position.
  ;; Usage example:  (set-position @!dsl :yellow-ball [5 0 0])
  (set-position [this name position])

  ;; Changes the color of the given object.
  ;; Usage example:  (set-color @!dsl :yellow-ball :green)
  (set-color [this name color]))
