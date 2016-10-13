(ns coerce.impl
  (:require [coerce.protocols :refer [Coerce->String
                                      Coerce->Boolean
                                      Coerce->Date
                                      Coerce->Integer
                                      Coerce->BigInteger
                                      Coerce->Decimal
                                      Coerce->BigDecimal
                                      Coerce->Keyword
                                      Coerce->UUID]]
            [coerce.constants :refer [+date-patterns+]]
            [coerce.utils :refer [clean-string extract-leading-integer extract-leading-decimal uuid-string? keywordify]]
            [clojure.string :as s]
            ;;
            [goog.i18n.DateTimeFormat :as dtf]
            [goog.i18n.DateTimeParse  :as dtp]))

;;; --------------------------------------------------------------------------------

(defn url-encode
  [s]
  (js/encodeURIComponent s))

;;; --------------------------------------------------------------------------------

(defn- read-number-string
  [s strict? throw? constructor cleaner]
  (when-let [v (some-> s clean-string constructor)]
    (if (and (number? v) (not (js/isNaN v)))
      v
      (if strict?
        (when throw? (throw (js/Exception. (str s " is not a number."))))
        (some-> s cleaner constructor)))))

;;; --------------------------------------------------------------------------------

(def +default-timezone+
  (->> (js/Date.)
       (.toString)
       (re-find #"([A-Z]+[\\+-][0-9]+)")
       first))

(defmulti string->date (fn [v pattern _] (if (or (keyword? pattern) (string? pattern)) :pattern pattern)))
(defmulti date->string (fn [v pattern _] (if (or (keyword? pattern) (string? pattern)) :pattern pattern)))

(defn- make-date-format
  [pattern]
  (goog.i18n.DateTimeFormat. (get +date-patterns+ pattern pattern)))

(defn- make-date-parse
  [pattern]
  (goog.i18n.DateTimeParse. (get +date-patterns+ pattern pattern)))

(defn parse-date
  ([v pattern timezone]
   (let [[v pattern] (if timezone
                       [(str v " " timezone) (str pattern " zzz")]
                       [v pattern])
         ;; Warning: Gotta create a date to populate, not functional.
         d (js/Date. 0)]
     (.parse (make-date-parse pattern) v d)
     d)))

(defn format-date
  [v pattern timezone]
  ;; FIXME: workout how to make use of timezone for formating.
  (.format (make-date-format pattern) v))

(defmethod string->date :pattern [v pattern timezone] (parse-date  v pattern timezone))
(defmethod date->string :pattern [v pattern timezone] (format-date v pattern timezone))

;;; --------------------------------------------------------------------------------

(extend-type string
  ;;
  Coerce->String
  (coerce->string
    [v {:keys [clean?] :or {clean? true} :as options}]
    (if clean?
      (clean-string v)
      v))
  ;;
  Coerce->Boolean
  (coerce->boolean
    [v {:keys [re-false?]
        :or   {re-false? #"(?i)^(f|false|n|no)$"}}]
    (if-let [v (clean-string v)]
      (if (re-find re-false? v)
        false
        true)
      false))
  ;;
  Coerce->Date
  (coerce->date
    [v {:keys [pattern timezone]
        :or   {pattern  :yyyy-mm-dd
               timezone +default-timezone+}
        :as   options}]
    (some-> v
            clean-string
            (string->date pattern timezone)))
  ;;

  Coerce->Integer
  (coerce->integer
    [v {:keys [strict? throw?]}]
    (read-number-string v strict? throw?
                        js/parseInt
                        extract-leading-integer))
  ;;
  Coerce->BigInteger
  (coerce->big-integer
    [v {:keys [strict? throw?]}]
    (read-number-string v strict? throw?
                        js/parseInt
                        extract-leading-integer))
  ;;
  Coerce->Decimal
  (coerce->decimal
    [v {:keys [strict? throw?]}]
    (read-number-string v strict? throw?
                        js/parseFloat
                        extract-leading-decimal))
  ;;
  Coerce->BigDecimal
  (coerce->big-decimal
    [v {:keys [strict? throw?]}]
    (read-number-string v strict? throw?
                        js/parseFloat
                        extract-leading-decimal))
  ;;
  Coerce->Keyword
  (coerce->keyword
    [v {:keys [strict?] :as options}]
    (if strict?
      (keyword v)
      (keywordify v)))
  ;;
  Coerce->UUID
  (coerce->uuid
    [v {:keys [throw?] :as options}]
    (when-let [v (clean-string v)]
      (if (uuid-string? v)
        ;; The clj #uuid reader lower cases but the cljs one doesn't,
        ;; make it lower case so that equality tests are consistent.
        (UUID. (s/lower-case v))
        (when throw? (throw (js/Exception. (str v " is not a UUID."))))))))

;;;

(extend-type number
  Coerce->String
  (coerce->string [v _] (str v)))

(extend-type js/Date
  Coerce->String
  (coerce->string
    [v {:keys [pattern timezone]
        :or   {pattern  :yyyy-mm-dd
               timezone +default-timezone+}
        :as options}]
    (date->string v pattern timezone))

  Coerce->Date
  (coerce->date [v o] v))

(extend-type Keyword
  Coerce->String  (coerce->string  [v _] (name v))
  Coerce->Keyword (coerce->keyword [v _] v))

(extend-type Symbol
  Coerce->String  (coerce->string  [v _] (name v)))

(extend-type cljs.core/UUID
  Coerce->String  (coerce->string [v _] (str v)))

;;;
