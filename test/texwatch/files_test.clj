(ns texwatch.files-test
  (:require [clojure.test :refer :all]
            [texwatch.files :as files :refer :all]))

(deftest directory-or-parent-test
  (testing "If presented with a directory reference, return it."
           (let [directory (files/get-file "data")]
             (is 
               (= directory (directory-or-parent directory)))))
  (testing "If presented with a file, return it's parent folder."
           (let [directory (files/get-file "data")
                 document (files/get-file "data/document.tex")]
             (is 
               (= directory (directory-or-parent document))))))
