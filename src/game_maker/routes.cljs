(ns game-maker.routes
  (:require [bidi.bidi :as bidi]
            [macchiato.util.response :as r]
            [hiccups.runtime]
            [game-maker.htmx.fragments :as htmx])
  (:require-macros [hiccups.core :as h]))

(defn- not-found [req res _raise]
  (-> (h/html
       [:html
        [:body
         [:h3 "Page \"" (:uri req) "\" was not found  ¯\\_(ツ)_/¯"]]])
      (r/not-found)
      (r/content-type "text/html")
      (res)))

(defn- html-page [html]
  (fn [_req res _raise]
    (-> (h/html html)
        (r/ok)
        (r/content-type "text/html")
        (res))))

(def ^:private routes
  ["/" {""           {:get (html-page htmx/index-page)}
        "clicked"    {:post (html-page htmx/clicked-result)}}])

(defn router [req res raise]
  (if-let [{:keys [handler route-params]} (bidi/match-route* routes (:uri req) req)]
    (handler (assoc req :route-params route-params) res raise)
    (not-found req res raise)))