(ns game-maker.utils)

(defmacro compile-time-slurp
  "When used in a ClojureScript file, this macro will return the contents of the file read at the compile time.
   The idea is from https://stackoverflow.com/a/51886724"
  [path]
  (slurp path))
