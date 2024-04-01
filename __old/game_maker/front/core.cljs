(ns game-maker.front.core
  (:require [htmx.org :as htmx]
            [game-maker.front.dsl]
            [game-maker.front.eval :as eval]
            [game-maker.front.babylon :as b2]))



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
  (b2/create-babylon!)

  ;; initialize OpenAI key and GPT model fields from the local storage
  (link-input-field-to-local-storage "openai-key")
  (link-input-field-to-local-storage "gpt-model")
  (link-input-field-to-local-storage "gpt-prompt"))

(defn -main []
  (eval/init #(js/console.log "[DEBUG] ClojureScript bootstrap environment initialized!"))
  
  (js/console.log "[DEBUG] Game Maker started!")

  (htmx/on "htmx:sendError" on-error)
  (htmx/on "htmx:responseError" on-error)

  ;; Initialize babylon.js once the page is loaded so the babylon can hookup with canvas HTML element. 
  ;; Note that we can't use "htmx:load" event as it is fired on every DOM change, including HTMX responses.
  (.addEventListener js/window "DOMContentLoaded" on-ready))

(defn ^:dev/after-load hot-reload []
  (-main)
  ;; need to start babylon after hot-reload as DOM "load" event is not triggered.
  (b2/create-babylon!))

(defn ^:dev/before-load close []
  (htmx/off "htmx:sendError" on-error)
  (htmx/off "htmx:responseError" on-error)
  (.removeEventListener js/window "DOMContentLoaded" on-ready)
  (b2/dispose-babylon!))
