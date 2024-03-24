(ns game-maker.front.dsl
  (:require [babylonjs :as bb]))


;; Stores references to  babylon engine and scene: {:engine ??, :scene ??, :resize-fn ??}
(def !babylon (atom nil))

(def ^:private !objects (atom nil))

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
  (reset! !objects nil)
  (js/console.log "[DEBUG] All scene objects are disposed."))

(defn- replace-object
  "Replaces the object with the given name with the new object."
  [objects name new-obj]
  (update objects name (fn [old-obj]
                         (when old-obj (.dispose old-obj))
                         new-obj)))
  

(defn ^:export create-sphere
  "Create a sphere with a given name at the given position with the given color."
  [name x y z diameter color]
  (println "-> Creating sphere with" name "name at" x y z "with diameter" diameter "and color" color)
  (let [sphere (bb/MeshBuilder.CreateSphere "sphere" #js {:diameter diameter} (current-scene))]
    (set! sphere.position (bb/Vector3. x y z))
    (set! sphere.material (bb/StandardMaterial. "sphereMat"))
    (set! sphere.material.diffuseColor (get colors color))
    (swap! !objects replace-object name sphere)
    nil))

(defn ^:export create-cuboid
  "Create a cuboid with a given name at the given position with the given color."
  [name x y z width height color]
  (println "-> Creating cuboid with" name "name at" x y z "with color" color)
  (let [cuboid (bb/MeshBuilder.CreateBox "cuboid" #js {:width width :height height, } (current-scene))]
    (set! cuboid.position (bb/Vector3. x y z))
    (set! cuboid.material (bb/StandardMaterial. "cuboidMat"))
    (set! cuboid.material.diffuseColor (get colors color))
    (swap! !objects replace-object name cuboid)
    nil))

(defn ^:export get-position
  "Returns position of the object with a given name as a vector [x y z]."
  [name]
  (println "-> Getting position of" name)
  (when-let [object (@!objects name)]
    (let [pos object.position]
      [(.-x pos) (.-y pos) (.-z pos)])))

(defn ^:export set-position
  "Set the position of the  object with a given name to the given position."
  [name x y z]
  (println "-> Setting position of" name "to" x y z)
  (when-let [object (@!objects name)]
    (set! object.position (bb/Vector3. x y z))
    nil))

(defn ^:export set-color 
  "Set the color of the object with a given name to the given color."
  [name color]
  (println "-> Setting color of" name "to" color)
  (when-let [object (@!objects name)]
    (set! object.material.diffuseColor (get colors color))
    nil))
  
(defn ^:export dispose
  "Disposes object with a given name."
  [name]
  (println "-> Disposing " name)
  (when-let [object (@!objects name)]
    (.dispose object)
    nil))
  