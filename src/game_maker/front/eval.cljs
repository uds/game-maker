(ns game-maker.front.eval
  (:require [cljs.js]
            [cljs.analyzer]
            [cljs.env]))

;; Links
;; https://stackoverflow.com/questions/51573858/how-can-i-run-eval-in-clojurescript-with-access-to-the-namespace-that-is-calling
;; https://gist.github.com/mfikes/66a120e18b75b6f4a3ecd0db8a976d84
;; https://code.thheller.com/blog/shadow-cljs/2017/10/14/bootstrap-support.html

;; https://gist.github.com/mfikes/66a120e18b75b6f4a3ecd0db8a976d84
;; (let [eval *eval*
;;       st (cljs.js/empty-state)]
;;   (set! *eval*
;;         (fn [form]
;;           (binding [cljs.env/*compiler* st
;;                     *ns* (find-ns cljs.analyzer/*cljs-ns*)
;;                     cljs.js/*eval-fn* cljs.js/js-eval]
;;             (eval form)))))


(defn evaluate [expr]
  (js/console.log "[DEBUG] Evaluating: " expr)
  (let [state (cljs.js/empty-state)
        opts {:eval    cljs.js/js-eval
              :context :expr
              :verbose false
              #_#_:ns 'cljs.core}
        callback println]
    (cljs.js/eval-str state expr nil opts callback)))

