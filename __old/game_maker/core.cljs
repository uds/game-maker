(ns game-maker.core
  (:require [macchiato.server :as server]
            [macchiato.middleware.defaults :as defaults]
            [game-maker.routes :as routes]))

(defn- wrap-server-defaults
  "Middleware configuration"
  [handler]
  (let [config (-> defaults/site-defaults
                   ;; override default static resources config
                   (assoc-in [:static :resources] "resources/public")
                   ;; disable anti-forgery middleware for this simple app
                   (assoc-in [:security :anti-forgery] false))]
    (defaults/wrap-defaults handler config)))

(def !server (atom nil))

(defn start
  "The server's PROD main entry point."
  []
  (let [host "0.0.0.0"
        port 3000]
    (reset! !server
            (server/start
             {:handler    (wrap-server-defaults routes/router)
              :host       host
              :port       port
              :on-success #(js/console.log "Server started on" (str host ":" port))}))))

(defn ^:dev/after-load after-load
  "This function is called by the shadow-cljs after the code is reloaded."
  []
  (js/console.log "** reload! **"))