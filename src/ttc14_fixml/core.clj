(ns ttc14-fixml.core
  (:require [clojure.string        :as str]
            [clojure.java.io       :as io]
            [stencil.core          :as s]
            [funnyqt.generic       :as gen]
            [funnyqt.emf           :as emf]
            [funnyqt.xmltg         :as xmltg]
            [funnyqt.model2model   :as m2m]
            [funnyqt.polyfns       :as poly]
            [funnyqt.tg            :as tg]
            [funnyqt.utils         :as u]))

;;* Solution

(tg/generate-schema-functions       "xml-schema.tg"  ttc14-fixml.xml xml)
(emf/generate-ecore-model-functions "model/oo.ecore" ttc14-fixml.oo  oo)

(def DATE    (emf/eenum-literal 'Builtins.DATE))
(def INTEGER (emf/eenum-literal 'Builtins.INTEGER))
(def LONG    (emf/eenum-literal 'Builtins.LONG))
(def DOUBLE  (emf/eenum-literal 'Builtins.DOUBLE))
(def STRING  (emf/eenum-literal 'Builtins.STRING))

;;** XML2OO

(m2m/deftransformation xml-graph2oo-model [[xml] [oo]]
  (^:top element2class
   :from [e '[:and Element !RootElement]]
   :to   [c (element-name2class (xml/name e))])
  (element-name2class
   :from [tag-name]
   :to   [c 'Class {:name tag-name}]
   (doseq [[an at av] (all-attributes tag-name)]
     (attribute2field an at av c))
   (doseq [[tag max-child-no] (all-children tag-name)]
     (children-of-same-tag2field tag max-child-no c))
   (when-let [char-conts (seq (all-character-contents tag-name))]
     (character-contents2field char-conts c)))
  (attribute2field
   :from [an at av class]
   :to   [f 'Field {:name an :type at :initialValue av :class class}])
  (children-of-same-tag2field
   :from [tag max-child-no class]
   :to   [f 'Field {:class class}]
   (if (= 1 max-child-no)
     (do (oo/set-name! f (str tag "_obj"))
         (oo/->set-type! f (element-name2class tag)))
     (let [array-type (oo/create-Array! oo :size max-child-no :elemType (element-name2class tag))]
       (oo/set-name! f (str tag "_objs"))
       (oo/->set-type! f array-type))))
  (character-contents2field
   :from [char-conts class]
   :to   [f 'Field {:name "Content" :initialValue (first char-conts)
                    :type (guess-type char-conts) :class class}])
  (guess-type [vals]
   (let [ts (set (map #(condp re-matches %
                         #"\d\d\d\d-\d\d-\d\d.*" DATE
                         #"[+-]?\d+\.\d+"        DOUBLE
                         #"[+-]?\d+"             (int-type %)
                         STRING) vals))]
     (get-or-create-builtin-type
      (cond
       (= (count ts) 1)              (first ts)
       (= ts #{DOUBLE INTEGER})      DOUBLE
       (= ts #{DOUBLE LONG})         DOUBLE
       (= ts #{DOUBLE LONG INTEGER}) DOUBLE
       (= ts #{INTEGER LONG})        LONG
       :else                         STRING))))
  (int-type [val]
   (try (and (Integer/parseInt val) INTEGER)
        (catch NumberFormatException _
          (try (and (Long/parseLong val) LONG)
               (catch NumberFormatException _
                 STRING)))))
  (get-or-create-builtin-type
   :from [t]
   :to   [bit 'Builtin {:type t}])
  (all-attributes [tag-name]
   (->> (elements-of-tag tag-name) (mapcat xml/->attributes) (group-by xml/name)
        (map (fn [[an as]]
               [an (guess-type (map xml/value as)) (xml/value (first as))]))))
  (elements-of-tag [tag-name]
   (->> (xml/vseq-Element xml) (filter #(= tag-name (xml/name %)))))
  (all-children [tag-name]
   (let [child-tag-names (->> (elements-of-tag tag-name) (mapcat xml/->children)
                              (map xml/name) (into #{}))]
     (map (fn [ctn] [ctn (max-children-of-tag tag-name ctn)]) child-tag-names)))
  (max-children-of-tag [tag-name child-tag-name]
   (reduce (fn [old el]
             (let [cot (count (filter #(= child-tag-name (xml/name %)) (xml/->children el)))]
               (if (> cot old) cot old)))
           0 (elements-of-tag tag-name)))
  (all-character-contents [tag-name]
   (->> (elements-of-tag tag-name) (mapcat xml/->charContents)
        (map xml/content) (remove str/blank?))))

(defn xml2oo [xml & more-xmls]
  (let [oo (emf/new-resource)
        dom (xmltg/xml2xml-graph xml)]
    (doseq [xml more-xmls]
      (xmltg/xml2xml-graph xml nil nil dom))
    (xml-graph2oo-model dom oo)
    oo))

;;** OO2Code

;;*** field-type

(def string-type {:java "String", :csharp "string", :cpp "std::string", :c "char*"})
(def timestamp-type {:java "Date", :csharp "DateTime", :cpp "std::tm", :c "struct tm"})

(defn object-type [class lang]
  (str (oo/name class) (when (#{:c :cpp} lang) "*")))

(defn field-type [type lang]
  (gen/type-case type
    'Class   (object-type type lang)
    'Builtin (condp = (oo/type type)
               DATE    (timestamp-type lang)
               DOUBLE  "double"
               INTEGER (case lang (:c :cpp) "long"      "int")
               LONG    (case lang (:c :cpp) "long long" "long")
               STRING  (string-type lang))
    'Array   (str (field-type (oo/->elemType type) lang)
                  (if (#{:c :cpp} lang) "*" "[]"))))

;;*** field-value-exp

(defn field-value-exp [field lang]
  (let [type (oo/->type field)
        val (oo/initialValue field)]
    (gen/type-case type
      'Class (if (= lang :c)
               (str "make_default_" (oo/name (oo/->type field)) "()")
               (str "new " (oo/name (oo/->type field)) "()"))
      'Builtin (condp = (oo/type type)
                 DATE   (case lang
                          :cpp (str "Util::parseDate(\"" val "\")")
                          :c   (str "parseDate(\"" val "\")")
                          (str "Util.parseDate(\"" val "\")"))
                 STRING (str "\"" val "\"")
                 LONG   (str val "L")
                 val)
      'Array (if (= lang :c)
               (format "(%s) make_pointer_array(%s, %s)"
                       (field-type type lang)
                       (oo/size type)
                       (str/join ", " (for [i (range (oo/size type))]
                                        (format "make_default_%s()"
                                                (oo/name (oo/->elemType type))))))
               (format "new %s[%s] {%s}"
                       (field-type (oo/->elemType type) lang)
                       (if (= lang :cpp) (oo/size type) "")
                       (str/join ", " (for [i (range (oo/size type))]
                                        (format "new %s()"
                                                (oo/name (oo/->elemType type))))))))))

;;*** file-ending

(defn file-ending
  ([lang] ({:java "java", :csharp "cs", :cpp "hpp", :c "h"} lang))
  ([lang impl]
     (if impl
       (case lang :cpp "cpp" :c "c")
       (file-ending lang))))

;;*** get-imports

(defn get-imports [cls lang]
  (letfn [(includes-from-type [t]
            (gen/type-case t
              'Class   (when (#{:c :cpp} lang)
                         [(format "\"%s.%s\"" (oo/name t) (file-ending lang))])
              'Builtin (condp = (oo/type t)
                         DATE   (case lang
                                    :java   ["java.util.Date"]
                                    :csharp ["System"]
                                    :cpp    ["<ctime>" "\"Util.hpp\""]
                                    :c      ["<time.h>" "\"Util.h\""])
                         STRING (when (= lang :cpp) ["<string>"])
                         nil)
              'Array   (concat (when (#{:c :cpp} lang)
                                 (includes-from-type (oo/->elemType t)))
                               (when (= lang :c)
                                 ["\"Util.h\""]))))]
    (map (fn [import] {:imported-class import})
         (set (mapcat includes-from-type (gen/adjs cls :fields :type))))))

;;*** to-mustache

(defn mark-first-field [l]
  (when (seq l)
    (cons (assoc (first l) :first true) (rest l))))

(poly/declare-polyfn to-mustache [el lang pkg])

(poly/defpolyfn to-mustache oo.Class [cls lang pkg]
  {:pkg-name pkg
   :imports (get-imports cls lang)
   :class-name (oo/name cls)
   :fields (mark-first-field (map #(to-mustache % lang pkg) (oo/->fields cls)))})

(poly/defpolyfn to-mustache oo.Field [f lang pkg]
  {:field-type (field-type (oo/->type f) lang)
   :field-name (oo/name f)
   :field-value-exp (field-value-exp f lang)
   :uscored-field-name (str "_" (oo/name f))
   :plain-field-type (let [t (oo/->type f)]
                       (gen/type-case t
                         'Array (oo/name (oo/->elemType t))
                         'Class (oo/name t)
                         nil))
   :pointer (gen/has-type? (oo/type f) 'Class)
   :array (gen/has-type? (oo/type f) 'Array)})

;;*** Emitting Code

(defn oo2code [oo pkg lang]
  (let [dir (format "results/%s/%s" (name lang) pkg)]
    (.mkdirs (io/file dir))
    (spit (format "%s/Util.%s" dir (file-ending lang))
          (s/render-file (format "templates/Util.%s" (file-ending lang))
                         {:pkg-name pkg}))
    (when (#{:c :cpp} lang)
      (spit (format "%s/Util.%s" dir (file-ending lang true))
            (s/render-file (format "templates/Util.%s" (file-ending lang true))
                           {:pkg-name pkg})))
    (doseq [cls (oo/eall-Classes oo)]
      (spit (format "%s/%s.%s" dir (oo/name cls) (file-ending lang))
            (s/render-file (format "templates/class.%s" (file-ending lang))
                           (to-mustache cls lang pkg)))
      (when (#{:c :cpp} lang)
        (spit (format "%s/%s.%s" dir (oo/name cls) (file-ending lang true))
              (s/render-file (format "templates/class.%s" (file-ending lang true))
                             (to-mustache cls lang pkg)))))))

;;* test code

(defn emit-code [pkg oo]
  (u/timing "  |-> Code generation time (Java):  %T" (oo2code oo pkg :java))
  (u/timing "  |-> Code generation time (C#):    %T" (oo2code oo pkg :csharp))
  (u/timing "  |-> Code generation time (C++):   %T" (oo2code oo pkg :cpp))
  (u/timing "  |-> Code generation time (C):     %T" (oo2code oo pkg :c)))

(defn wellformed? [xml]
  (try (xmltg/xml2xml-graph xml)
       true
       (catch Exception _
         (println "File" xml "is not well-formed. Skipping...")
         false)))

(defn -main [& _]
  (let [xmls (sort (filter wellformed? (map #(.getPath %) (.listFiles (io/file "messages/")))))]
    (apply xml2oo xmls) ;; a little warmup
    (doseq [xml xmls]
      (let [pkg (second (re-find #"([a-zA-z0-9_]+).xml" (.getName (io/file xml))))]
        (println "* Transforming" xml "\n \\")
        (u/timing "  |-> Overall evaluation time:      %T\n  `---"
                  (let [oo (u/timing "  |-> Transformation time:          %T"
                                     (xml2oo xml))]
                    (emit-code pkg oo)))))
    (println "* Transforming all FIXML messages into one model\n \\")
    (u/timing "  |-> Overall evaluation time:      %T\n  `---"
              (let [oo (u/timing "  |-> Transformation time:          %T"
                                 (apply xml2oo xmls))]
                (emit-code "fixml_complete" oo)))))

