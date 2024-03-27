(ns game-maker.front.babylon
  (:require [babylonjs :as bb]
            [cannon :as cannon]))


;; Stores references to the babylon engine and scene: {:engine ??, :scene ??, :resize-fn ??}
(def ^:private !babylon (atom nil))


(defn- create-camera
  "Create a camera and return it."
  [scene]
  (bb/ArcRotateCamera. "arc-camera" (* Math/PI 1.5) (* Math/PI 0.5) 50 (bb/Vector3. 0 0 0) scene))

(defn create-babylon!
  "Creates babylon engine and scene."
  []
  (let [canvas (.getElementById js/document "renderCanvas")
        engine (bb/Engine. canvas true)
        scene  (bb/Scene. engine)
        camera (create-camera scene)]

    ;; Prevents bubbling of the wheel events to the parent element so the parent doesn't scroll when mouse is over the canvas.
    ;; No event removal is needed on close as the event handler will be destroyed when the canvas is removed from the DOM.
    (.addEventListener canvas "wheel" (fn [e] (.preventDefault e)))

    (.attachControl camera canvas true)

    (let [domeLight (bb/HemisphericLight. "light" (bb/Vector3. 1 5 -1))]
      (set! domeLight.intensity 0.9))

    ;; initialize physics
    (set! js/window.CANNON cannon)
    (.enablePhysics scene)
    (.. scene getPhysicsEngine (setGravity (bb/Vector3. 0 0.00000001 0)))

    (.runRenderLoop engine (fn [] (.render scene)))

    (let [resize-fn #(.resize engine)]
      (.addEventListener js/window "resize" resize-fn)
      (reset! !babylon {:engine    engine
                        :scene     scene
                        :resize-fn resize-fn}))))

(defn dispose-babylon!
  "Disposes babylon engine and scene."
  []
  (when-let [{:keys [engine resize-fn]} @!babylon]
    (js/console.log "[DEBUG] babylon.js disposed")
    (.dispose engine)
    (.removeEventListener js/window "resize" resize-fn)
    (reset! !babylon nil)))

(defn current-scene
  "Returns the current babylon scene."
  []
  (:scene @!babylon))

(defn vec3 
  "Converts a vector [x y z] to a babylon Vector3 object."
  [[x y z]]
  (bb/Vector3. x y z))

(defn size3-js
  "Converts a size vector [height width depth] to a babylon Vector3 object."
  [[height width depth]]
  #js {:height height :width width :depth depth})
