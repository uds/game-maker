(ns game-maker.gpt
  (:require [openai :refer [OpenAI]]
            ["fs" :as fs]
            [clojure.string :as str]))

(def ^:private !openai-key (atom nil))
(def ^:private !openai (atom nil))

(def !chat-history (atom nil))


(defn- slurp [path]
  (.toString
   (fs/readFileSync path #js {:encoding "utf8" :flag     "r"})))


(defn- gpt-instructions []
  (str
   "
You are a code generator AI.
You will be asked to generate a code fragments given the instructions provided by a user at the end.
   - The domain is a game development.
   - The origin point is a center of the canvas.
   - The position in space is defined as a vector [x y z].
   - The position of the objects is defined by x, y, z coordinates and is located at the center of the mass of the object.
   - The generated code should be in ClojureScript.
   - Do not comment the generated code.
   - Enclose generated code into ''' quotes.

Use game engine API functions described below (enclosed into ''' quotes) to generate the resulting code fragment so that:
'''
" 
   (slurp "src/game_maker/front/dsl_api.cljs")
   "  
'''
IMPORTANT:
   - Only use standard ClojureScript functions and macros.
   - Do not define any new functions, always inline code.
   - Do not use newly defined variables to pass values into DSL functions.
   - Do not repeat already generated code parts.
"))


(defn- init-openai! [api-key]
  (when (not= api-key @!openai-key)
    (reset! !openai-key api-key)
    (reset! !openai nil))
  (if-let [openai @!openai]
    openai
    (reset! !openai (OpenAI. #js {:apiKey api-key}))))

(defn clear-chat-history []
  (reset! !chat-history nil))

(defn execute
  "Returns a response from the OpenAI chat API as a promise."
  [{:keys [api-key model prompt]}]
  (let [openai ^js (init-openai! api-key)
        params {:model       model
                :temperature 0            ;; temperature is the randomness of the output
                :top_p       1            ;; top_p is the probability of the model's output
                :messages    [{:role    "system"
                               :content (gpt-instructions)}
                              {:role    "system"
                               :content (str "Following is a conversation history - the generated so far program (enclosed in ''' quotes): \n"
                                             "'''" @!chat-history "'''")}
                              {:role    "user"
                               :content prompt}]}]
    (-> (.. openai -chat -completions (create (clj->js params)))
        (.then (fn [result]
                 (let [content (-> (get-in (js->clj result :keywordize-keys true)
                                           [:choices 0 :message :content])
                                   (str/replace #"'''" "")
                                   (str/replace #"```clojure" "")  ;; TODO: sometimes gpt generates code enclosed in ```clojure quotes
                                   (str/replace #"```" "")         ;; TODO: sometimes gpt generates code enclosed in ``` quotes
                                   (str/trim))
                       prompt* (->> (str/split-lines prompt)
                                    (map #(str ";; user> " %))
                                    (str/join "\n"))]
                   (swap! !chat-history #(str % prompt* "\n" content "\n\n"))
                   [content @!chat-history]))))))
  