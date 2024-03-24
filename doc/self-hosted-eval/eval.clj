(ns game-maker.front.eval
  (:require [cljs.env :as env]))

(defmacro analyzer-state [[_ ns-sym]]
  `'~(get-in @env/*compiler* [:cljs.analyzer/namespaces ns-sym]))

(defmacro analyzer-state-namespaces []
  `'~(:cljs.analyzer/namespaces @env/*compiler*))
