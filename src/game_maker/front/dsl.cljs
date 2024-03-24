(ns game-maker.front.dsl
  (:require [babylonjs :as bb]))


;; Stores references to  babylon engine and scene: {:engine ??, :scene ??, :resize-fn ??}
(def !babylon (atom nil))

(def ^:private !objects (atom {}))

(def ^:private colors {:red     (bb/Color3. 1 0 0)
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


(defn- current-scene
  "Returns the current babylon scene."
  []
  (:scene @!babylon))

(defn ^:export dispose-all
  "Disposes all objects."
  []
  (doseq [obj (vals @!objects)]
    (.dispose obj))
  (reset! !objects {})
  (js/console.log "[DEBUG] All scene objects are disposed."))

(defn- replace-object
  "Replaces the object with the given name with the new object."
  [objects name new-obj]
  (update objects name (fn [old-obj]
                         (when old-obj (.dispose old-obj))
                         new-obj)))

(defn- get-object
  "Returns the object with the given name."
  [name]
  (if-let [obj (@!objects name)]
    obj
    (throw (js/Error. (str "Object with name " name " not found.")))))
  

(defn ^:export create-sphere
  "Create a sphere with a given name at the given position with the given color."
  [name x y z diameter color]
  (js/console.log "-> Creating sphere with" name "name at" x y z "with diameter" diameter "and color" color)
  (let [sphere (bb/MeshBuilder.CreateSphere "sphere" #js {:diameter diameter} (current-scene))]
    (set! sphere.position (bb/Vector3. x y z))
    (set! sphere.material (bb/StandardMaterial. "sphereMat"))
    (set! sphere.material.diffuseColor (get colors color))
    (swap! !objects replace-object name sphere)
    nil))

(defn ^:export create-cuboid
  "Create a cuboid with a given name at the given position with the given color."
  [name x y z width height color]
  (js/console.log "-> Creating cuboid with" name "name at" x y z "with color" color)
  (let [cuboid (bb/MeshBuilder.CreateBox "cuboid" #js {:width width :height height, } (current-scene))]
    (set! cuboid.position (bb/Vector3. x y z))
    (set! cuboid.material (bb/StandardMaterial. "cuboidMat"))
    (set! cuboid.material.diffuseColor (get colors color))
    (swap! !objects replace-object name cuboid)
    nil))

(defn ^:export get-position
  "Returns position of the object with a given name as a vector [x y z]."
  [name]
  (js/console.log "-> Getting position of" name)
  (let [pos (.-position (get-object name))]
    [(.-x pos) (.-y pos) (.-z pos)]))

(defn ^:export set-position
  "Set the position of the  object with a given name to the given position."
  [name x y z]
  (js/console.log "-> Setting position of" name "to" x y z)
  (let [object (get-object name)]
    (set! object.position (bb/Vector3. x y z))
    nil))

(defn ^:export set-color 
  "Set the color of the object with a given name to the given color."
  [name color]
  (js/console.log "-> Setting color of" name "to" color)
  (let [object (get-object name)]
    (set! object.material.diffuseColor (get colors color))
    nil))
  
(defn ^:export dispose
  "Disposes object with a given name."
  [name]
  (js/console.log "-> Disposing " name)
  (.dispose (get-object name))
  nil)
  