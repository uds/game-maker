(ns game-maker.htmx.fragments)

(defn- send-icon [{class :class}]
  [:svg {:xmlns "http://www.w3.org/2000/svg"
         :class  class
         :fill   "none"
         :viewBox "0 0 24 24"
         :stroke "currentColor"
         :stroke-width "1.5"}
   [:path {:stroke-linecap "round"
           :stroke-linejoin "round"
           :d "M6 12 3.269 3.125A59.769 59.769 0 0 1 21.485 12 59.768 59.768 0 0 1 3.27 20.875L5.999 12Zm0 0h7.5"}]])

(defn- send-button []
  [:button {:class      "px-4 py-2 rounded-md border border-transparent shadow-md text-base font-medium focus:outline-none focus:ring-2 focus:ring-offset-2 text-white bg-neutral-500 hover:bg-neutral-600 focus:ring-neutral-500"
            :type       "button"
            :hx-post    "/clicked2"
            :hx-trigger "click"
            :hx-target  "#parent-div"
            :hx-swap    "outerHTML"}
   [:span {:class "sr-only"} "Send"]
   [:div {:class "flex"}
    (send-icon {:class "h-6 w-6 mr-2"})
    "Send"]])

(def index-page
  [:html
   [:head
    [:title "Game Maker"]

    ;; app styles
    [:link {:rel "stylesheet", :href "/css/compiled/front-styles.css"}]]

   [:body {:class "px-8"}
    [:script {:src  "/js/compiled/app-front.js"
              :type "text/javascript"}]
    [:h1 "Game Maker"]
    [:p "Welcome to Game Maker!"]
    [:div#parent-div
     (send-button)]]])

(def clicked-result
  [:p "You clicked the button!"])
