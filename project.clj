(defproject texwatch "0.1.0"
  :description 
  "Watch a folder with .tex files for changes. Generate a PDF from
the main document.tex when a change occurs. Should work with miktex out of 
the box. Requires 'latex' and 'biber' binaries to be locatable on the path."
  
  :url "https://github.com/fourcube/texwatch"
  :license {:name "MIT"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [clojure-watch "LATEST"]
                 [org.clojure/tools.cli "0.3.1"]
                 [pandect "0.5.2"]]
  :main ^:skip-aot texwatch.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
