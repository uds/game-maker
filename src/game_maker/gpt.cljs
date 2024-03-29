(ns game-maker.gpt
  (:require [openai :refer [OpenAI]]
            [clojure.string :as str]))

(def ^:private !openai-key (atom nil))
(def ^:private !openai (atom nil))


;; Stores the chat history as a list of records. Each record is a map with keys :role, :content and :answer.
(def !chat-history (atom nil))


(def ^:private gpt-instructions
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
   - Only use standard ClojureScript functions and macros.
   - Do not define any new functions, always inline code.
   - Do not repeat already generated code parts.

'''
;; ------------------------------------------------------------------------------------------------------------
;; For all functions in this API:
;;
;;  - The object name is a keyword.
;;  - The color parameter in all functions is a keyword.
;;    Supported color values are :red, :green, :blue, :yellow, :magenta, :cyan, :white, :black, :gray, :purple, :orange
;; ------------------------------------------------------------------------------------------------------------

;; Returns a random floating number between the low and high (exclusive) numbers.
(defn rand-range [low high])

;; Create a sphere with a given name at the given position with the given color.
;; Usage example:  (create-sphere :yellow-ball -5 0 0 2 :yellow)
(defn create-sphere [name x y z diameter color])

;; Create a cuboid with a given name at the given position with the given dimensions and color.
;; Usage example:  (create-cuboid :yellow-brick -5 0 0 1 2 2 :yellow)
(defn create-cuboid [name x y z height width depth color])

;; Returns position of the object with a given name as a vector [x y z].
;; Usage example:  (get-position :yellow-ball) ; => [-5 0 0]
(defn get-position [name])

;; Set the position of the object with a given name to the given position.
;; Usage example:  (set-position :yellow-ball 5 0 0)
(defn set-position [name x y z])

;; Changes color of the given object.
;; Usage example:  (set-color :yellow-ball :green)
(defn set-color [name color])

;; Disposes object with a given name.
(defn dispose [name])
'''
")


(defn- init-openai! [api-key]
  (when (not= api-key @!openai-key)
    (reset! !openai-key api-key)
    (reset! !openai nil))
  (if-let [openai @!openai]
    openai
    (reset! !openai (OpenAI. #js {:apiKey api-key}))))

(defn clear-chat-history []
  (reset! !chat-history nil))

(defn- extract-code [result]
  (-> (get-in (js->clj result :keywordize-keys true)
              [:choices 0 :message :content])
      (str/replace #"'''" "")
      (str/replace #"```clojure" "")  ;; sometimes gpt generates code enclosed in ```clojure quotes
      (str/replace #"```" "")         ;; sometimes gpt generates code enclosed in ``` quotes
      (str/trim)))

(defn execute
  "Returns a response from the OpenAI chat API as a promise."
  [{:keys [api-key model prompt]}]
  (let [openai ^js (init-openai! api-key)
        instructions gpt-instructions
        params {:model       model
                :temperature 0            ;; temperature is the randomness of the output
                :top_p       1            ;; top_p is the probability of the model's output
                :messages    [{:role    "system"
                               :content instructions}
                              {:role    "system"
                               :content (str "Following is a conversation history - the generated so far program (enclosed in ''' quotes): \n"
                                             "'''" @!chat-history "'''")}
                              {:role    "user"
                               :content prompt}]}]
    (-> (.. openai -chat -completions (create (clj->js params)))
        (.then (fn [result]
                 (let [content (extract-code result)
                       prompt* (->> (str/split-lines prompt)
                                    (map #(str ";; user> " %))
                                    (str/join "\n"))]
                   (swap! !chat-history #(str % prompt* "\n" content "\n\n"))
                   [content @!chat-history]))))))
  