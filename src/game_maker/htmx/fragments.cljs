(ns game-maker.htmx.fragments
  (:require [game-maker.htmx.widgets :as w]
            [game-maker.gpt :as gpt]))

(defn index-page []
  [:html
   [:head
    [:title "Game Maker"]

    [:meta {:charset "utf-8"}]
    [:meta {:name    "viewport"
            :content "width=device-width,initial-scale=1,maximum-scale=1"}]

    ;; app styles
    [:link {:rel  "stylesheet"
            :href "/css/compiled/front-styles.css"}]]

   [:body
    [:script {:src  "/js/compiled/app-front.js"
              :type "text/javascript"}]

    [:div {:class "mx-auto max-w-screen-2xl h-full py-8 px-4 sm:px-8 "}
     ;; title
     [:h1 {:class "text-3xl font-semibold text-neutral-500"} "Game Maker"]
     [:p "Welcome to Game Maker!"]

     [:div {:class "mt-8 sm:h-[80vh] flex flex-col gap-8 sm:grid sm:grid-cols-2 sm:gap-4"}
      ;; Babylon canvas. Wrapper DIV is needed to make relative resizing of the canvas work. 
      ;; On the babylon.js side, engine.resize() is called on window resize to match engine size to the parent DIV size.
      [:div {:class "flex-1"}
       [:canvas {:id    "renderCanvas"
                 :class "h-full w-full max-h-[80vh]  border rounded-md"}]]

      [:form {:class "flex-1 flex flex-col gap-4 m-0 "}
       ;; LLM response panel
       [:div#query-result {:class "flex-1 p-2 max-h-96 overflow-auto border rounded-md border-neutral-300"}
        (if-let [chat-history @gpt/!chat-history]
          [:p {:class "whitespace-pre text-indigo-400"} chat-history]
          [:span {:class "italic text-neutral-400"} "Results will be shown here..."])]

       ;; TODO: validate input params !!!!
       
       ;; prompt params
       [:div {:class "flex flex-col sm:flex-row gap-4"}
        (w/input {:class       "flex-1"
                  :label       "OpenAI key"
                  :name        "openai-key"
                  :placeholder "enter your OpenAI key..."})
        (w/input {:class       "flex-1"
                  :label       "GPT model"
                  :name        "gpt-model"
                  :placeholder "enter the model name, e.g. gpt-3.5-turbo..."})]

       [:p {:class "text-sm italic text-teal-600"}
        "* You can change the OpenAI key and GPT model name at any time. The values will only be stored in the browser's local storage and NEVER on the server."]

       ;; prompt text
       (w/textarea {:label "Prompt"
                    :name  "gpt-prompt"
                    :rows  5})

       [:dive {:class "flex justify-end gap-4"}
        (w/button {:label       "Clear"
                   :icon        (w/clear-icon {:class "button-icon h-6 w-6 mr-2"})
                   :hx-post     "/clear"
                   :hx-target   "#query-result"
                   :hx-swap     "innerHTML"
                   :hx-on:click "game_maker.dsl.dispose_all()"})     ;; FIXME: a hack to dispose generated object on a scene
        (w/button {:label     "Send"
                   :icon      (w/send-icon {:class "button-icon h-6 w-6 mr-2"})
                   :hx-post   "/send"
                   :hx-target "#query-result"
                   :hx-swap   "innerHTML scroll:bottom"})]]]]]])

(defn send-prompt
  "Sends the prompt to the OpenAI chat API and returns the response.
   It is a standard HTTP router handler function that accepts request, response and raise triple as parameters."
  [req res _raise]
  (let [{:keys [openai-key gpt-model gpt-prompt]} (:params req)]
    (-> (gpt/execute {:api-key openai-key
                      :model   gpt-model
                      :prompt  gpt-prompt})
        (.then (fn [[code chat-history]]
                 (-> [:span
                      [:p {:class "whitespace-pre text-indigo-400"} chat-history]
                      [:span#query-code-result {:class "hidden"} code]]
                     (res))))
        (.catch (fn [err]
                  (-> [:p {:class "text-red-500"} err]
                      (res)))))))

(defn clear-chat-history []
  (gpt/clear-chat-history)
  [:span {:class "italic text-neutral-400"} "Results will be shown here..."])
