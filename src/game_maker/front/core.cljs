(ns game-maker.front.core
  (:require [htmx.org :as htmx]
            [babylonjs :as bb]
            [game-maker.front.eval :as eval]
            [game-maker.front.dsl :as dsl]))


(defn- create-camera
  "Create a camera and return it."
  [scene]
  (bb/ArcRotateCamera. "arc-camera" (* Math/PI 1.5) (* Math/PI 0.5) 50 (bb/Vector3. 0 0 0) scene))

(defn- create-babylon!
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

    (.runRenderLoop engine (fn [] (.render scene)))

    (let [resize-fn #(.resize engine)]
      (.addEventListener js/window "resize" resize-fn)
      (reset! dsl/!babylon {:engine    engine
                        :scene     scene
                        :resize-fn resize-fn}))))

(defn- dispose-babylon!
  "Disposes babylon engine and scene."
  []
  (when-let [{:keys [engine resize-fn]} @dsl/!babylon]
    (js/console.log "[DEBUG] babylon.js disposed")
    (.dispose engine)
    (.removeEventListener js/window "resize" resize-fn)
    (reset! dsl/!babylon nil)))

;; ---------------------------------------------------------

(defn- on-error [event]
  (js/alert (str "Error: " (js->clj event.detail :keywordize-keys true))))

(defn- link-input-field-to-local-storage 
  "Links the input field with the given id to the local storage. 
   The value will be read from storage on page load and stored on 'blur' event of the input box."
  [id]
  (let [el (js/document.getElementById id)]
    (.addEventListener el "blur" (fn [event]
                                   (js/localStorage.setItem id (.. event -target -value))))
    (set! (.-value el) (js/localStorage.getItem id))))

(defn- on-ready []
  (create-babylon!)

  ;; initialize OpenAI key and GPT model fields from the local storage
  (link-input-field-to-local-storage "openai-key")
  (link-input-field-to-local-storage "gpt-model")
  (link-input-field-to-local-storage "gpt-prompt"))

(defn -main []
  (eval/init #(js/console.log "[DEBUG] ClojureScript bootstrap environment initialized!"))
  (.then (dsl/init) (js/console.log "[DEBUG] DSL API initialized!"))

  (js/console.log "[DEBUG] Game Maker started!")

  (htmx/on "htmx:sendError" on-error)
  (htmx/on "htmx:responseError" on-error)

  ;; Initialize babylon.js once the page is loaded so the babylon can hookup with canvas HTML element. 
  ;; Note that we can't use "htmx:load" event as it is fired on every DOM change, including HTMX responses.
  (.addEventListener js/window "DOMContentLoaded" on-ready))

(defn ^:dev/after-load hot-reload []
  (-main)
  ;; need to start babylon after hot-reload as DOM "load" event is not triggered.
  (create-babylon!))

(defn ^:dev/before-load close []
  (htmx/off "htmx:sendError" on-error)
  (htmx/off "htmx:responseError" on-error)
  (.removeEventListener js/window "DOMContentLoaded" on-ready)
  (dispose-babylon!))
