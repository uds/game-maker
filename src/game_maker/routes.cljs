(ns game-maker.routes
  (:require [bidi.bidi :as bidi]
            [macchiato.util.response :as r]
            [hiccups.runtime]
            [game-maker.htmx.fragments :as f])
  (:require-macros [hiccups.core :as h]))

(defn- not-found [req res _raise]
  (-> (h/html
       [:html
        [:body
         [:h3 "Page \"" (:uri req) "\" was not found  ¯\\_(ツ)_/¯"]]])
      (r/not-found)
      (r/content-type "text/html")
      (res)))

(def ^:private routes
  ["/" {""      {:get f/index-page}
        "send"  {:post f/send-prompt}
        "clear" {:post f/clear-chat-history}}])

(defn router [req res raise]
  (if-let [{:keys [handler route-params]} (bidi/match-route* routes (:uri req) req)]
    (handler (assoc req :route-params route-params) res raise)
    (not-found req res raise)))