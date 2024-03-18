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

(defn- html-response [html]
  (-> (h/html html)
      (r/ok)
      (r/content-type "text/html")))

(defn- html-page-wrapper
  "A wrapper for a handler function that converts result Hiccup data to HTML page before passing it to 'res' callback."
  [handler]
  (fn [req res raise]
    (handler req #(res (html-response %)) raise)))

(defn- html-page [html-fn]
  (fn [req res _raise]
    (-> (html-response (html-fn req))
        (res))))

(def ^:private routes
  ["/" {""       {:get (html-page f/index-page)}
        "send"   {:post (html-page-wrapper f/send-prompt)}
        "clear"  {:post (html-page f/clear-chat-history)}}])

(defn router [req res raise]
  (if-let [{:keys [handler route-params]} (bidi/match-route* routes (:uri req) req)]
    (handler (assoc req :route-params route-params) res raise)
    (not-found req res raise)))