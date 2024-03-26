(ns game-maker.front.dsl
  (:require [babylonjs :as bb]
            ["@babylonjs/havok":as HavokPlugin]
            [game-maker.front.dsl-api :as api]))


;; Stores references to the babylon engine and scene: {:engine ??, :scene ??, :resize-fn ??}
(def !babylon (atom nil))


(defn- current-scene
  "Returns the current babylon scene."
  []
  (:scene @!babylon))

(defn- vec3 
  "Converts a vector [x y z] to a babylon Vector3 object."
  [[x y z]]
  (bb/Vector3. x y z))

(defn- size3-js
  "Converts a size vector [height width depth] to a babylon Vector3 object."
  [[height width depth]]
  #js {:height height :width width :depth depth})

(defn dispose-all 
  "Called by the GUI Clear button handler"
  []
  (api/dispose-all @api/!dsl))


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
    (let [sphere (bb/MeshBuilder.CreateSphere "sphere" #js {:diameter diameter} (current-scene))]
      (set! sphere.position (vec3 position))
      (set! sphere.material (bb/StandardMaterial. "sphereMat"))
      (set! sphere.material.diffuseColor (get api/colors color))
      (.set-object! this name sphere)
      nil))

  (create-cuboid ^:export [this name position size color]
    (js/console.log "-> Creating cuboid with" name "name at " position " with " size " dimensions and color" color)
    (let [cuboid (bb/MeshBuilder.CreateBox "cuboid" (size3-js size) (current-scene))]
      (set! cuboid.position (vec3 position))
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
      (set! object.position (vec3 position))
      nil))

  (set-color ^:export [this name color]
    (js/console.log "-> Setting color of" name "to" color)
    (let [object (.get-object this name)]
      (set! object.material.diffuseColor (get api/colors color))
      nil)))

;; Creates an instance of the DSL API
(reset! api/!dsl (Dsl. {}))

(defn init
  "Initializes the DSL API. The function is asynchronous and Returns a promise."
  []
  ;; FIXME: the HavokPhysics.wasm file has to be copied to the public folder (e.g., /public/assets) 
  ;;        in order to be loaded by the browser during the plugin initialization.
  ;; Load the Havok physics plugin
  (HavokPlugin. #js {:locateFile (fn [path] 
                                   (str "/assets/" path))}))