(ns game-maker.htmx.widgets
  (:require [clojure.string :as str]))

(defn- svg-icon [{:keys [class]
                  :as   params} path-d]
  [:svg (merge {:xmlns        "http://www.w3.org/2000/svg"
                :class        class
                :fill         "none"
                :viewBox      "0 0 24 24"
                :stroke       "currentColor"
                :stroke-width "1.5"}
               params)
   [:path {:stroke-linecap "round"
           :stroke-linejoin "round"
           :d path-d}]])

(defn send-icon [params]
  (svg-icon (merge params {:stroke-width "1"})
            "M6 12 3.269 3.125A59.769 59.769 0 0 1 21.485 12 59.768 59.768 0 0 1 3.27 20.875L5.999 12Zm0 0h7.5"))

(defn clear-icon [params]
  (svg-icon params "M6 18 18 6M6 6l12 12"))

(defn undo-icon [params]
  (svg-icon params "M9 15 3 9m0 0 6-6M3 9h12a6 6 0 0 1 0 12h-3"))

(defn redo-icon [params]
  (svg-icon params "m15 15 6-6m0 0-6-6m6 6H9a6 6 0 0 0 0 12h3"))

(defn retry-icon [params]
  (svg-icon params "M19.5 12c0-1.232-.046-2.453-.138-3.662a4.006 4.006 0 0 0-3.7-3.7 48.678 48.678 0 0 0-7.324 0 4.006 4.006 0 0 0-3.7 3.7c-.017.22-.032.441-.046.662M19.5 12l3-3m-3 3-3-3m-12 3c0 1.232.046 2.453.138 3.662a4.006 4.006 0 0 0 3.7 3.7 48.656 48.656 0 0 0 7.324 0 4.006 4.006 0 0 0 3.7-3.7c.017-.22.032-.441.046-.662M4.5 12l3 3m-3-3-3 3"))

(defn spinner [{class :class}]
  [:svg {:xmlns       "http://www.w3.org/2000/svg"
         :class       class
         :aria-hidden "true"
         :role        "status"
         :viewBox     "0 0 100 101"
         :fill        "none"}
   [:path {:fill "currentFill"
           :d    "M100 50.5908C100 78.2051 77.6142 100.591 50 100.591C22.3858 100.591 0 78.2051 0 50.5908C0 22.9766 22.3858 0.59082 50 0.59082C77.6142 0.59082 100 22.9766 100 50.5908ZM9.08144 50.5908C9.08144 73.1895 27.4013 91.5094 50 91.5094C72.5987 91.5094 90.9186 73.1895 90.9186 50.5908C90.9186 27.9921 72.5987 9.67226 50 9.67226C27.4013 9.67226 9.08144 27.9921 9.08144 50.5908Z"}]
   [:path {:fill "currentColor"
           :d    "M93.9676 39.0409C96.393 38.4038 97.8624 35.9116 97.0079 33.5539C95.2932 28.8227 92.871 24.3692 89.8167 20.348C85.8452 15.1192 80.8826 10.7238 75.2124 7.41289C69.5422 4.10194 63.2754 1.94025 56.7698 1.05124C51.7666 0.367541 46.6976 0.446843 41.7345 1.27873C39.2613 1.69328 37.813 4.19778 38.4501 6.62326C39.0873 9.04874 41.5694 10.4717 44.0505 10.1071C47.8511 9.54855 51.7191 9.52689 55.5402 10.0491C60.8642 10.7766 65.9928 12.5457 70.6331 15.2552C75.2735 17.9648 79.3347 21.5619 82.5849 25.841C84.9175 28.9121 86.7997 32.2913 88.1811 35.8758C89.083 38.2158 91.5421 39.6781 93.9676 39.0409Z"}]])

(defn button [{:keys [class label icon tooltip] :as params}]
  (let [hx-params (into {} (filter #(str/starts-with? (name (first %)) "hx-") params))]
    [:button (merge {:class           (str "has-tooltip px-4 py-2 rounded-md border border-transparent shadow-md text-base font-medium focus:outline-none focus:ring-2 focus:ring-offset-2 text-white bg-neutral-500 hover:bg-neutral-600 focus:ring-neutral-500" " "
                                           class)
                     :type            "button"
                     :hx-trigger      "click"
                     :hx-indicator    "find .button-indicator"
                     :hx-disabled-elt "this"}
                    hx-params)
     [:span {:class "sr-only"} "Send"]
     
     (when tooltip
       [:span {:class "tooltip hidden sm:inline rounded border shadow-lg p-2 bg-gray-100 font-normal text-neutral-700 -ml-16 -mt-16"} 
        tooltip])
     
     [:div {:class "flex"}
      [:div {:class "button-indicator relative "}
       icon
       (spinner {:class "htmx-indicator absolute top-0 left-0 h-6 w-6 text-white fill-neutral-400 animate-spin"})]
      (when label 
        [:span {:class "ml-2 hidden md:inline"} label])]]))

(defn input [{:keys [class label name placeholder auto-complete]}]
  [:div {:class class}
   [:label {:for   name
            :class "block text-sm font-medium text-neutral-700"}
    label]
   [:input {:id            name
            :class         "form-input mt-1 block w-full rounded-md border-neutral-300 shadow-sm focus:border-neutral-400 focus:ring-neutral-500 sm:text-sm"
            :type          "text"
            :name          name
            :placeholder   placeholder
            :auto-complete auto-complete}]])


(defn textarea [{:keys [class label name rows]}]
  [:div {:class class}
   [:label {:for   name
            :class "block text-sm font-medium text-neutral-700"}
    label]
   [:textarea {:id    name
               :class "form-textarea mt-1 block w-full rounded-md border-neutral-300 shadow-sm focus:border-neutral-400 focus:ring-neutral-400 sm:text-sm"
               :name  name
               :rows  (or rows 3)}]])
