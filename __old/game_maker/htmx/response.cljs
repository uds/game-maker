(ns game-maker.htmx.response
  (:require [macchiato.util.response :as r])
  (:require-macros [hiccups.core :as h]))

(defn ok 
  "Takes Hiccup data and returns a Ring response with 200 status code and text/html content type."
  [hiccup]
  (-> (str "<!DOCTYPE html>\n" (h/html hiccup))
      (r/ok)
      (r/content-type "text/html")))

(defn trigger-event
  "Triggers client-side event by sending an HX-Trigger header with the event name."
  [resp event]
  (->> (if (map? event)
         (js/JSON.stringify (clj->js event))
         (str (clj->js event)))
       (r/header resp "HX-Trigger")))