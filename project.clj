(defproject ttc14-fixml "0.1.0-SNAPSHOT"
  :description "The FunnyQT solution to the TTC'14 FIXML case."
  :url "https://github.com/tsdh/ttc14-fixml"
  :license {:name "GNU General Public License, Version 3 (or later)"
            :url "http://www.gnu.org/licenses/gpl.html"
            :distribution :repo}
  :dependencies [[funnyqt "0.20.1"]
                 [stencil "0.3.3"]]
  :main ^:skip-aot ttc14-fixml.core
  ;; :global-vars {*warn-on-reflection* true}
  :jvm-opts ^:replace ["-Xms512M" "-Xmx512M"])
