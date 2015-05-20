(ns texwatch.core
  (:gen-class)
  (:require  [clojure.tools.cli :refer [parse-opts]]    
             [clojure.string :as str]
             [texwatch.files :refer :all] 
             [texwatch.processing :refer :all]))

(def extension-whitelist [#"\.tex$"
                          #"\.bib$"
                          #"\.png$"
                          #"\.jpg$"
                          #"\.jpeg$"])

(defn bind-invoke-tex
  "Return a function that invokes latex on input file."
  [input-file output-folder]
  (let [input-file (clojure.string/replace input-file "\\" "\\\\")]
    (fn [event path]
      (cond 
        (some? (current-event-path)) nil
        (not-any? #(re-find % path) extension-whitelist) nil
        (not (has-changed? path)) nil
        :else (invoke-tex input-file path output-folder)))))

(def cli-options
  [["-t" "--temporary-folder" "Temporary file output folder."
   :default nil
   ]])

(defn -main "
Takes a filepath as it's sole argument. 
Registers a recursive watcher on the filepaths containing directory. 

Whenever a watched file changes, that matches `extension-whitelist`, 
a latex -> biber -> latex command is run on on the filepath.
"
  [& args]
  (let [opts (parse-opts args cli-options)
        file (first (get opts :arguments))
        ; Files must be .tex files.
        _ (when (not (.endsWith file ".tex")) (throw (java.lang.RuntimeException. (str file " is no .tex file!"))))
        
        absolutePath (.getAbsolutePath (get-file file))
        output-folder (.getAbsolutePath (as-folder file))
        callback (bind-invoke-tex absolutePath output-folder)]
    
    ; Process once
    (process-document absolutePath output-folder)
    
    ; Init a new watcher and save the stop-fn
    (create-watcher file callback)))
