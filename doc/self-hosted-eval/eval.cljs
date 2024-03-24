(ns game-maker.front.eval
  (:require [cljs.js :as cjs]
            [cljs.env :as env]
            [cljs.analyzer :as ana])
  (:require-macros [game-maker.front.eval :as e]))

;; Links
;; https://stackoverflow.com/questions/51573858/how-can-i-run-eval-in-clojurescript-with-access-to-the-namespace-that-is-calling
;; https://gist.github.com/mfikes/66a120e18b75b6f4a3ecd0db8a976d84
;; !!! self-hosting bootstrap with shadow-cljs >>> https://code.thheller.com/blog/shadow-cljs/2017/10/14/bootstrap-support.html
;; How to list all macros (not working): https://gist.github.com/philoskim/1d61574f69902c102d1a3c5c9112c6ba
;; getting-started-with-self-hosted-cljs: http://nbeloglazov.com/2016/03/05/getting-started-with-self-hosted-cljs-part-1.html

;; https://gist.github.com/mfikes/66a120e18b75b6f4a3ecd0db8a976d84
;; (let [eval *eval*
;;       st (cljs.js/empty-state)]
;;   (set! *eval*
;;         (fn [form]
;;           (binding [cljs.env/*compiler* st
;;                     *ns* (find-ns cljs.analyzer/*cljs-ns*)
;;                     cljs.js/*eval-fn* cljs.js/js-eval]
;;             (eval form)))))



;; see https://github.com/mfikes/ambient
(defn init-state [state]
  (-> state
      (assoc-in [:cljs.analyzer/namespaces 'cljs.core] (e/analyzer-state 'cljs.core))
      (assoc-in [:cljs.analyzer/namespaces 'cljs.js] (e/analyzer-state 'cljs.js))   ;; NOTE that this makes cljs.core macros available to eval!!
      (assoc-in [:cljs.analyzer/namespaces 'game-maker.front.dsl] (e/analyzer-state 'game-maker.front.dsl))))

;; Returns reference to the cljs state atom
(def ^:private compile-state (cjs/empty-state init-state))

(defn _eval [form]
  (binding [env/*compiler* compile-state
            *ns* (find-ns ana/*cljs-ns*)
            cjs/*eval-fn* cjs/js-eval]
    (*eval* form)))

(defn evaluate [expr]
  (js/console.log "[DEBUG] Evaluating: " expr)
  (let [state    compile-state
        opts     {:eval    cjs/js-eval
                  :context :statement
                  :ns      'game-maker.front.dsl
                  :verbose false}
        callback identity]
    (cljs.js/eval-str state expr "expr" opts callback)))

(defn evaluate* [form]
  (js/console.log "[DEBUG] Evaluating: " form)
  (let [state    compile-state
        opts     {:eval    cjs/js-eval
                  :context :expr
                  ;;:ns      'game-maker.front.eval
                  :verbose false}
        callback identity]
    (cljs.js/eval state form opts callback)))

(defn compile [expr]
  (let [state    compile-state
        opts     {:eval cjs/js-eval}
        callback println]
    (cljs.js/compile-str state expr nil opts callback)))
