(ns texwatch.processing
  (:require [pandect.core :as pandect :refer [sha1]]
            [clojure.java.shell :refer [sh]]
            [clojure-watch.core :refer [start-watch]]

            [texwatch.files :refer :all]))

(defonce current-event (atom {:path nil}))
(defonce current-watcher (atom nil))
(defonce file-hash-register (atom {}))

(defn create-watcher
  "Registers a callback that will be invoked when something inside path recursively changes."
  [path callback]
  (let [path (as-folder path)
        stop-watch-fn (start-watch [{:path path
                   :event-types [:create :modify]
                   :bootstrap (fn [path] (println "Starting to watch" path))
                   :callback (fn [event path] (println event path) (callback event path))
                   :options {:recursive true}}])]

    (reset! current-watcher stop-watch-fn)))

(defn terminate-watcher
  "Terminates the current watcher if there is one."
  []
  (when-let [stop-watch-fn (deref current-watcher)]
    (stop-watch-fn)))

(defn get-hash [f]
  (->> f
    (get-file)
    (pandect/sha1)))

(defn get-last-known-hash
  [f]
  (get (deref file-hash-register) f))

(defn has-changed?
  "Returns true when the contents of the file have changed."
  [f]
  (not= (get-hash f) (get-last-known-hash f)))

(defn current-event-path
  []
  (:path (deref current-event)))

(defn sh-latex
  "Run latex as a shell command."
  [document-file output-folder]
  (let [output-directory-option (str "-output-directory=" output-folder)]
    (sh "latex" "-output-format=pdf" "--enable-installer" output-directory-option document-file)))

(defn sh-biber
  "Run biblatex biber as a shell command."
  [document-file output-folder]
  (let [input-folder (as-folder document-file)
        input-folder-option (str "-input-directory=" input-folder)
        biber-config-file (clojure.string/replace document-file ".tex" ".bcf")]
    (sh "biber" input-folder-option biber-config-file)))

(defn process-document
  "Process the input-file tex and write the result to the output-folder."
  [input-file output-folder]
  (time
    (let [result (assoc {} :latex-preprocess-run (sh-latex input-file output-folder))
          result (assoc result :biber-run (sh-biber input-file output-folder))
          result (assoc result :latex-postprocess-run (sh-latex input-file output-folder))]
      result)))

(defn invoke-tex
  [input-file changed-path output-folder]

  (swap! file-hash-register assoc changed-path (get-hash changed-path))
  (reset! current-event {:path changed-path})
  (process-document input-file output-folder)
  (reset! current-event {:path nil}))
