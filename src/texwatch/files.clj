(ns texwatch.files
  (:require [clojure.java.io :refer [as-file]]))

(defn get-file
  "Just a wrapper around `clojure.java.io/as-file`"
  [f]
  (as-file f))

(defn directory-or-parent
  [f]
  (if (.isDirectory f)
    f
    (.getParentFile f)))

(defn as-folder
  "Converts f to a java.io.File that represents the closest parent folder."
  [f]
  (->> f 
    (as-file)
    (.getAbsoluteFile)
    (directory-or-parent)))