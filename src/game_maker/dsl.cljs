(ns game-maker.dsl
  (:require [babylonjs :as bb]
            [game-maker.dsl-api :as api]
            [game-maker.babylon :as b2]))



(defn ^:export reset
  "Called by the GUI Clear button handler"
  []
  (api/dispose-all @api/!dsl)
  (b2/set-gravity b2/zero-gravity-vector))


;; ---------------------------------------------------------
;; API functions


(deftype Dsl [objects]
  Object

  (get-object [this name]
    (if-let [obj (get this.objects name)]
      obj
      (throw (js/Error. (str "Object with name " name " not found.")))))

  (set-object! [this name obj]
    (->> (update this.objects name (fn [old-obj]
                                     (when old-obj (.dispose old-obj))
                                     obj))
         (set! this.objects)))

  api/DslApi

  (dispose ^:export [this name]
    (js/console.log "-> Disposing " name)
    (.dispose (.get-object this name))
    nil)

  (dispose-all ^:export [this]
    (doseq [obj (vals this.objects)]
      (.dispose obj))
    (set! this.objects {})
    (js/console.log "[DEBUG] All scene objects are disposed.")
    nil)

  (create-sphere ^:export [this name position diameter color]
    (js/console.log "-> Creating sphere with" name "name at " position " with diameter" diameter "and color" color)
    (let [scene (b2/current-scene)
          sphere (bb/MeshBuilder.CreateSphere "sphere" #js {:diameter diameter} scene)
          imposter (bb/PhysicsImpostor. sphere bb/PhysicsImpostor.SphereImpostor #js {:mass 1 :restitution 0.5} scene)]
      (set! sphere.physicsImpostor imposter)
      (set! sphere.position (b2/vec3 position))
      (set! sphere.material (bb/StandardMaterial. "sphereMat"))
      (set! sphere.material.diffuseColor (get api/colors color))
      (.set-object! this name sphere)
      nil))

  (create-cuboid ^:export [this name position size color]
    (js/console.log "-> Creating cuboid with" name "name at " position " with " size " dimensions and color" color)
    (let [scene (b2/current-scene)
          cuboid (bb/MeshBuilder.CreateBox "cuboid" (b2/size3-js size) scene)
          imposter (bb/PhysicsImpostor. cuboid bb/PhysicsImpostor.BoxImpostor #js {:mass 0 :restitution 0.5} scene)]
      (set! cuboid.physicsImpostor imposter)
      (set! cuboid.position (b2/vec3 position))
      (set! cuboid.material (bb/StandardMaterial. "cuboidMat"))
      (set! cuboid.material.diffuseColor (get api/colors color))
      (.set-object! this name cuboid)
      nil))

  (get-position ^:export [this name]
    (js/console.log "-> Getting position of" name)
    (let [pos (.. this (get-object name) -position)]
      [(.-x pos) (.-y pos) (.-z pos)]))

  (set-position ^:export [this name position]
    (js/console.log "-> Setting position of" name "to" position)
    (let [object (.get-object this name)]
      (set! object.position (b2/vec3 position))
      nil))

  (set-color ^:export [this name color]
    (js/console.log "-> Setting color of" name "to" color)
    (let [object (.get-object this name)]
      (set! object.material.diffuseColor (get api/colors color))
      nil))

  (set-gravity ^:export [_this gravity-vector]
    (js/console.log "-> Setting gravity to" gravity-vector)
    (b2/set-gravity (b2/vec3 gravity-vector))
    nil))

;; Creates an instance of the DSL API
(reset! api/!dsl (Dsl. {}))
