(ns game-maker.dsl
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
  [name x y z color]
  (println "-> Creating sphere with" name "name at" x y z "with color" color)
  (let [sphere (bb/MeshBuilder.CreateSphere "sphere" #js {:diameter 2.0} (current-scene))]
    (set! sphere.position (bb/Vector3. x y z))
    (set! sphere.material (bb/StandardMaterial. "sphereMat"))
    (set! sphere.material.diffuseColor (get colors color))
    (swap! !objects replace-object name sphere)
    nil))

(defn ^:export set-position
  "Set the position of the  object with a given name to the given position."
  [name x y z]
  (println "-> Setting position of" name "to" x y z)
  (when-let [object (@!objects name)]
    (set! object.position (bb/Vector3. x y z))
    nil))
  
(defn ^:export dispose
  "Disposes object with a given name."
  [name]
  (println "-> Disposing " name)
  (when-let [object (@!objects name)]
    (.dispose object)
    nil))
  