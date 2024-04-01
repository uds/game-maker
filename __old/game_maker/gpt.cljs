(ns game-maker.gpt
  (:require [openai :refer [OpenAI]]
            ["fs" :as fs]
            [clojure.string :as str]))

(def ^:private !openai-key (atom nil))
(def ^:private !openai (atom nil))


;; Stores the chat history as a list of messages, where message is a map with keys :role and :content.
(def !chat-history (atom nil))


(defn- slurp [path]
  (.toString
   (fs/readFileSync path #js {:encoding "utf8" :flag     "r"})))


(defn- gpt-instructions []
  (str
   "
You are a code generator AI.
You will be asked to generate a code fragments given the instructions provided by the user.
    
The target domain is a game development within the 3D game engine, where:
   - The origin point is a center of the canvas.
   - The x-axis is pointing to the right, the y-axis is pointing forward, and the z-axis is pointing up.
   - The position in space is defined as a vector [x y z].
   - The position of the objects is defined by x, y, z coordinates and is located at the center of the mass of the object.
    
You will be using a special ClojureScript DSL to generate the code:
   - The generated code should be in ClojureScript.
   - Do not comment the generated code.
   - Only use standard ClojureScript functions and macros.
   - Do not define any new functions, always inline code.
   - Do not use newly defined variables to pass values into DSL functions.
   - Do not repeat already generated code parts.
   - Do not include any explanations into the answer.

Following is a ClojureScript file that defines the DSL API, use it to generate the code:
"
   (slurp "src/game_maker/front/dsl_api.cljs")))


(defn- init-openai! [api-key]
  (when (not= api-key @!openai-key)
    (reset! !openai-key api-key)
    (reset! !openai nil))
  (if-let [openai @!openai]
    openai
    (reset! !openai (OpenAI. #js {:apiKey api-key}))))

(defn clear-chat-history []
  (reset! !chat-history nil))

(defn- extract-message [result]
  (get-in (js->clj result :keywordize-keys true)
          [:choices 0 :message]))

(defn- update-history! [prompt answer]
  (->> (conj (vec @!chat-history) prompt answer)
       (reset! !chat-history)))

(defn- extract-code [message]
  (-> (:content message)
      (str/replace #"'''" "")
      (str/replace #"```clojure" "")  ;; sometimes gpt generates code enclosed in ```clojure quotes
      (str/replace #"```" "")         ;; sometimes gpt generates code enclosed in ``` quotes
      (str/trim)))

(defn execute
  "Returns a response from the OpenAI chat API as a promise."
  [{:keys [api-key model prompt]}]
  (let [openai     ^js (init-openai! api-key)
        prompt-msg {:role    "user"
                    :content prompt}
        params     {:model       model
                    :temperature 0            ;; temperature is the randomness of the output
                    :top_p       1            ;; top_p is the probability of the model's output
                    :messages    (concat [{:role    "system"
                                           :content (gpt-instructions)}]
                                         @!chat-history
                                         [prompt-msg])}]
    (-> (.. openai -chat -completions (create (clj->js params)))
        (.then (fn [result]
                 (let [answer-msg (extract-message result)
                       code       (extract-code answer-msg)
                       history    (update-history! prompt-msg 
                                                   (assoc answer-msg :content code))]
                   [code history]))))))
  