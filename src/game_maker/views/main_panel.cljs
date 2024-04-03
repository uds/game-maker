(ns game-maker.views.main-panel
  (:require [game-maker.views.widgets :as w]))

(defn- format-chat-history [chat-history]
  [:p
   (map (fn [{:keys [role content]}]
          (if (= role "user")
            [:div {:class ""}
             [:span {:class "font-semibold italic"} (str role ": ")]
             [:span {:class "text-teal-600"} (str content)]]
            [:div {:class "mb-4"}
             [:div {:class "font-semibold italic"} (str role ": ")]
             [:div {:class "whitespace-pre text-indigo-400"} (str content)]]))
        chat-history)])


(defn main-panel []
  [:div {:class "mx-auto _max-w-screen-2xl h-full py-8 px-4 sm:px-8"}

   ;; title
   [:h1 {:class "text-3xl font-semibold text-neutral-500"} "Game Maker"]
   [:p "Welcome to Game Maker!"]

   [:div {:class "mt-8 sm:h-[80vh] flex flex-col gap-8 sm:grid sm:grid-cols-2 sm:gap-4 md:gap-8 lg:gap-12"}
    ;; Babylon canvas. Wrapper DIV is needed to make relative resizing of the canvas work. 
    ;; On the babylon.js side, engine.resize() is called on window resize to match engine size to the parent DIV size.
    [:div {:class "flex-1"}
     [:canvas {:id    "renderCanvas"
               :class "h-full w-full max-h-[80vh]  border rounded-md"}]]

    [:form {:class "flex-1 flex flex-col gap-4 m-0"}
     ;; LLM response panel
     [:div#query-result {:class "flex-1 p-2 max-h-80 overflow-auto border rounded-md border-neutral-300"}
      (if-let [chat-history nil #_@gpt/!chat-history]

        ;; FIXME: sanitize GPT answers!!! 
        [format-chat-history chat-history]
        [:span {:class "italic text-neutral-400"} "Results will be shown here..."])]

     ;; prompt params
     [:div {:class "flex flex-col sm:flex-row gap-4"}
      [w/input {:class       "flex-1"
                :label       "OpenAI key"
                :name        "openai-key"
                :placeholder "enter your OpenAI key..."}
       (js/localStorage.getItem "openai-key")]
      [w/input {:class       "flex-1"
                :label       "GPT model"
                :name        "gpt-model"
                :placeholder "e.g. gpt-3.5-turbo-0125 or gpt-4-0125-preview..."}
       (js/localStorage.getItem "gpt-model")]]

     [:p {:class "text-sm italic text-teal-600"}
      "* You can change the OpenAI key and GPT model name at any time. The values will only be stored in the browser's local storage and NEVER on the server."]

     ;; prompt text
     [w/textarea {:label "Prompt"
                  :name  "gpt-prompt"
                  :rows  3}]

     [:div {:class "flex justify-between gap-2 sm:gap-4"}
      [:div {:class "flex gap-2 sm:gap-4"}
       [w/button {:tooltip "Undo the last action."
                  :icon    [w/undo-icon {:class "button-icon h-6 w-6"}]}]
       [w/button {:tooltip "Redo the last action."
                  :icon    [w/redo-icon {:class "button-icon h-6 w-6"}]}]
       [w/button {:tooltip "Retry the last action."
                  :icon    [w/retry-icon {:class "button-icon h-6 w-6"}]}]]

      [:div {:class "flex gap-2 sm:gap-4"}
       [w/button {:label                    "Clear"
                  :tooltip                  "Clear the canvas and chat history."
                  :icon                     [w/clear-icon {:class "button-icon h-6 w-6"}]}]

       [w/button {:label                       "Send"
                  :tooltip                     "Send the prompt to the OpenAI chat API."
                  :icon                        [w/send-icon {:class "button-icon h-6 w-6"}]}]]]]]])