(ns game-maker.gpt
  (:require [openai :refer [OpenAI]]
            ["fs" :as fs]
            [clojure.string :as str]))

(def ^:private instructions-file "src/game_maker/gpt-instructions.txt")

(def ^:private !openai-key (atom nil))
(def ^:private !openai (atom nil))

(def !chat-history (atom nil))


(defn- init-openai! [api-key]
  (when (not= api-key @!openai-key)
    (reset! !openai-key api-key)
    (reset! !openai nil))
  (if-let [openai @!openai]
    openai
    (reset! !openai (OpenAI. #js {:apiKey api-key}))))

(defn- slurp [path]
  (.toString 
   (fs/readFileSync path #js {:encoding "utf8"
                              :flag     "r"})))

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
                               :content (str/join "\n"
                                                  ["You are a code generator AI."
                                                   "You will be asked to generate a code fragments given the specification in a user's prompt."
                                                   " - The domain is a game development."
                                                   " - The origin point is a center of canvas."
                                                   " - The generated code should be in ClojureScript."
                                                   " - Do not comment the generated code."
                                                   ;;" - Add comments to to the generated code."
                                                   ;;" - Store created objects into variables."
                                                   ;;" - Generate only single statement at a time."
                                                   ;;" - Enclose multiple generated s-forms into a (do ...) form."
                                                   " - Enclose generated code into ''' quotes."
                                                   ""
                                                   "Use the following game engine API methods (enclosed into ''' quotes) to generate resulting code fragment.
                                                    Do not repeat already generated code!"
                                                   ""
                                                   "'''"
                                                   ";; For all functions in API:"
                                                   ";; The object name is a keyword."
                                                   ";; The color parameter in all functions is a keyword."
                                                   ";; Supported values are :red, :green, :blue, :yellow, :magenta, :cyan, :white, :black, :gray, :purple, :orange"
                                                   ""
                                                   ";; Create a sphere with a given name at the given position with the given color."
                                                   ";; The typical diameter value is 2."
                                                   ";; Usage example:  (game-maker.dsl/create-sphere :yellow-ball -5 0 0 2 :yellow)"
                                                   "(defn game-maker.dsl/create-sphere [name x y z diameter color])"
                                                   ""
                                                   ";; Create a cuboid with a given name at the given position with the given color."
                                                   ";; The typical width is 4 and height is 2."
                                                   ";; Usage example:  (game-maker.dsl/create-cuboid :yellow-brick -5 0 0 4 2 :yellow)"
                                                   "(defn game-maker.dsl/create-cuboid [name x y z width height color])"
                                                   ""
                                                   ";; Set the position of the object with a given name to the given position."
                                                   ";; Usage example:  (game-maker.dsl/set-position :yellow-ball 5 0 0)"
                                                   "(defn game-maker.dsl/set-position [name x y z])"
                                                   ""
                                                   ";; Disposes object with a given name."
                                                   "(defn game-maker.dsl/dispose [name])"
                                                   "'''"])}
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
                                   (str/trim))]
                   (swap! !chat-history #(str % content "\n\n"))

                   ;; TODO: sometimes gpt generates multiple s-forms that has to be enclosed into do form
                   [(str "(do " content ")"), @!chat-history]))))))
  