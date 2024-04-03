(ns game-maker.views.main-panel
  (:require [react :as r]
            [game-maker.views.widgets :as w]
            [game-maker.dsl :as dsl]
            [game-maker.gpt :as gpt]
            [game-maker.eval :as evl]))

(defn- format-chat-history [chat-history]
  (into [:div#query-result-panel]
        (map (fn [{:keys [role content]}]
               (cond
                 (= role "user")     [:div
                                      [:span {:class "font-semibold italic"} (str role ": ")]
                                      [:span {:class "text-teal-600"} (str content)]]
                 (= role "error")    [:div {:class "mb-4"}
                                      [:div {:class "font-semibold italic"} (str role ": ")]
                                      [:div {:class "whitespace-pre text-red-500"} (str content)]]
                 :else               [:div {:class "mb-4"}
                                      [:div {:class "font-semibold italic"} (str role ": ")]
                                      [:div {:class "whitespace-pre text-indigo-400"} (str content)]]))
             chat-history)))

(defn- input-and-store [widget-fn params value set-value]
  (let [name  (:name params)]
    (widget-fn (merge params {:value     value
                              :on-change #(let [v (.. % -target -value)]
                                            (js/localStorage.setItem name v)
                                            (set-value v))}))))

(defn- scroll-to-bottom [el delay]
  (when el
    ;; lazy: just use a delay to ensure that DOM was re-rendered before scrolling
    (js/setTimeout #(.scrollIntoView el #js {:block "end"
                                             :behavior "smooth"})
                   (or delay 0))))

(defn- show-error [message chat-history set-chat-history]
  (set-chat-history (conj (vec chat-history) {:role    "error"
                                              :content message})))


;; TODO: simplify, less params
(defn- exec-prompt
  "Executes GPT prompt and returns the chat history."
  [openai-key gpt-model prompt chat-history set-chat-history set-waiting?]
  (set-waiting? true)
  (-> (gpt/execute {:api-key openai-key
                    :model   gpt-model
                    :prompt  prompt})
      (.then (fn [[code history]]
               (try
                 (evl/evaluate code)
                 (set-chat-history history)
                 ;; TODO: all places where history is updated should be scrolled-to-bottom
                 ;; FIXME: make result-panel as react component
                 (scroll-to-bottom (js/document.getElementById "query-result-panel") 100)
                 (catch js/Error err
                   (show-error (.-message err) chat-history set-chat-history)))
               (set-waiting? false)))
      (.catch (fn [err]
                (show-error err chat-history set-chat-history)
                (set-waiting? false)))))

(defn- clear-result [set-chat-history]
  (dsl/reset)
  (gpt/clear-chat-history)
  (set-chat-history nil))


(defn main-panel []
  (let [[openai-key set-openai-key]     (r/useState (js/localStorage.getItem "openai-key"))
        [gpt-model set-gpt-model]       (r/useState (js/localStorage.getItem "gpt-model"))
        [prompt set-prompt]             (r/useState (or (js/localStorage.getItem "gpt-prompt") "Hi!"))
        [chat-history set-chat-history] (r/useState @gpt/!chat-history)
        [waiting? set-waiting?]         (r/useState false)]
    [:div {:class "mx-auto _max-w-screen-2xl h-full py-8 px-4 sm:px-8"}

     ;; title
     [:h1 {:class "text-3xl font-semibold text-neutral-500"} "Game Maker"]
     [:p "Welcome to Game Maker! Here you can create games using the power of AI. Just type in your ideas and see them come to life!"]
     [:p "See the full project sources on "
      [:a {:class "text-blue-600 hover:text-blue-400"
           :href  "https://github.com/uds/game-maker"} "GitHub"]
      "."]

     [:div {:class "mt-8 sm:h-[80vh] flex flex-col gap-8 sm:grid sm:grid-cols-2 sm:gap-4 md:gap-8 lg:gap-12"}
      ;; Babylon canvas. Wrapper DIV is needed to make relative resizing of the canvas work. 
      ;; On the babylon.js side, engine.resize() is called on window resize to match engine size to the parent DIV size.
      [:div {:class "flex-1"}
       [:canvas {:id    "renderCanvas"
                 :class "h-full w-full max-h-[80vh]  border rounded-md"}]]

      [:form {:class "flex-1 flex flex-col gap-4 m-0"}
       ;; LLM response panel
       [:div {:class "flex-1 p-2 max-h-80 overflow-auto border rounded-md border-neutral-300"}
        (if chat-history
          [format-chat-history chat-history]
          [:span {:class "italic text-neutral-400"} "Results will be shown here..."])]

       ;; prompt params
       [:div {:class "flex flex-col sm:flex-row gap-4"}
        [input-and-store w/input {:class       "flex-1"
                                  :label       "OpenAI key"
                                  :name        "openai-key"
                                  :placeholder "enter your OpenAI key..."} openai-key set-openai-key]
        [input-and-store w/input {:class       "flex-1"
                                  :label       "GPT model"
                                  :name        "gpt-model"
                                  :placeholder "e.g. gpt-3.5-turbo-0125 or gpt-4-0125-preview..."} gpt-model set-gpt-model]]

       [:p {:class "text-sm italic text-teal-600"}
        "* You can change the OpenAI key and GPT model name at any time. The values will only be stored in the browser's local storage and NEVER on the server."]

       ;; prompt text
       [input-and-store w/textarea {:label     "Prompt"
                                    :name      "gpt-prompt"
                                    :rows      3
                                    :value     prompt
                                    :on-change #(set-prompt (.. % -target -value))} prompt set-prompt]

       [:div {:class "flex justify-between gap-2 sm:gap-4"}
        [:div {:class "flex gap-2 sm:gap-4"}
         [w/button {:tooltip "Undo the last action."
                    :icon    [w/undo-icon {:class "button-icon h-6 w-6"}]}]
         [w/button {:tooltip "Redo the last action."
                    :icon    [w/redo-icon {:class "button-icon h-6 w-6"}]}]
         [w/button {:tooltip "Retry the last action."
                    :icon    [w/retry-icon {:class "button-icon h-6 w-6"}]}]]

        [:div {:class "flex gap-2 sm:gap-4"}
         [w/button {:label    "Clear"
                    :tooltip  "Clear the canvas and chat history."
                    :icon     [w/clear-icon {:class "button-icon h-6 w-6"}]
                    :on-click #(clear-result set-chat-history)}]

         [w/button {:label    "Send"
                    :tooltip  "Send the prompt to the OpenAI chat API."
                    :icon     [w/send-icon {:class "button-icon h-6 w-6"}]
                    :waiting? waiting?
                    :on-click #(exec-prompt openai-key gpt-model prompt chat-history set-chat-history set-waiting?)}]]]]]]))

;; TODO: remove gpt/history atom or do not write error to it - history in atom and on screen have discrepancies in case of error.
;; TODO: sanitize GPT prompt and answers!!! 
;; TODO: error handling
;; TODO: scroll to the bottom of the chat history on update - make it as React component
