(ns coerce.impl
  (:require [clojure.string :as s]
            [coerce.protocols :refer [Coerce->String
                                      Coerce->Boolean
                                      Coerce->Date
                                      Coerce->Integer
                                      Coerce->BigInteger
                                      Coerce->Decimal
                                      Coerce->BigDecimal
                                      Coerce->Keyword
                                      Coerce->UUID]]
            [coerce.constants :refer [+date-patterns+]]
            [coerce.utils :refer [clean-string extract-leading-integer extract-leading-decimal keywordify]])
  (:import  [java.util TimeZone UUID]
            [java.text SimpleDateFormat]
            ;;
            [java.math BigInteger BigDecimal]
            [java.net URLEncoder URLDecoder]))

;;; --------------------------------------------------------------------------------

(defn url-encode
  [s]
  (URLEncoder/encode s))

;;; --------------------------------------------------------------------------------

(defn- read-number-string
  [s strict? throw? constructor cleaner]
  (when-let [s (clean-string s)]
    (try
      (constructor s)
      (catch Exception e
        (if strict?
          (when throw? (throw e))
          (some-> s cleaner constructor))))))

;;; --------------------------------------------------------------------------------

(defmulti date->string (fn [v pattern] (if (or (keyword? pattern) (string? pattern)) :pattern pattern)))
(defmulti string->date (fn [v pattern] (if (or (keyword? pattern) (string? pattern)) :pattern pattern)))

(defn- make-date-format
  [pattern]
  (doto (SimpleDateFormat. (get +date-patterns+ pattern pattern))
    (.setTimeZone (TimeZone/getTimeZone "UTC"))
    (.setLenient false)))

(defmethod date->string :pattern
  [v pattern]
  (.format (make-date-format pattern) v))

(defmethod string->date :pattern
  [v pattern]
  (.parse  (make-date-format pattern) v))

;;; --------------------------------------------------------------------------------

(extend-type java.lang.String
  ;;
  Coerce->String
  (coerce->string
    [v {:keys [clean?]
        :as options
        :or {clean? true}}]
    (if clean?
      (clean-string v)
      v))
  ;;
  Coerce->Boolean
  (coerce->boolean
    [v {:keys [re-false?]
        :or   {re-false? #"(?i)^(f|false|n|no)$"}}]
    ;; By default tried to get semantics close to NIL, in other words
    ;; blank strings, 'f' or 'false', 'n' or 'no' are false,
    ;; everything else is true.
    (if-let [v (clean-string v)]
      (if (re-find re-false? v)
        false
        true)
      false))
  ;;
  Coerce->Date
  (coerce->date
    [v {:keys [pattern]}]
    (some-> v clean-string (string->date (or pattern :yyyy-mm-dd))))
  ;;
  Coerce->Integer
  (coerce->integer
    [v {:keys [strict? throw? constructor]
        :or   {constructor #(Long/parseLong %)}
        :as   options}]
    ;; Can pass #(Integer/parseInt %) if you really need it, or cast
    ;; after the fact.
    (read-number-string v strict? throw?
                        constructor
                        extract-leading-integer))
  ;;
  Coerce->BigInteger
  (coerce->big-integer
    [v {:keys [strict? throw?] :as options}]
    (read-number-string v strict? throw?
                        #(BigInteger. %)
                        extract-leading-integer))
  ;;
  Coerce->Decimal
  (coerce->decimal
    [v {:keys [strict? throw? constructor]
        :or   {constructor #(Float/parseFloat %)}
        :as options}]
    ;; Can pass #(Double/parseDouble %) if you really need it, or cast
    ;; after the fact.
    (read-number-string v strict? throw?
                        constructor
                        extract-leading-decimal))
  ;;
  Coerce->BigDecimal
  (coerce->big-decimal
    [v {:keys [strict? throw?] :as options}]
    (read-number-string v strict? throw?
                        #(BigDecimal. %)
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
      (try
        (UUID/fromString v)
        (catch Exception e
          (when throw? (throw e)))))))

;;;

(extend-type java.lang.Boolean
  Coerce->String  (coerce->string  [v {:keys [t f] :or {t "true" f "false"}}] (if v t f))
  Coerce->Integer (coerce->integer [v {:as options}] (if v 1 0)))

;;; --------------------------------------------------------------------------------
;;  Dates are a total hassle, try to do the 'sane' thing here.

(extend-type java.util.Date
  Coerce->String
  (coerce->string
    [v {:keys [pattern]}]
    (date->string v (or pattern :yyyy-mm-dd)))

  Coerce->Date
  (coerce->date [v o] v))

;;;

(extend-type java.lang.Integer
  Coerce->String     (coerce->string      [v _] (str v))
  Coerce->Integer    (coerce->integer     [v _] v)
  Coerce->BigInteger (coerce->big-integer [v _] (bigint v))
  ;;
  Coerce->Decimal    (coerce->decimal     [v _] (float  v))
  Coerce->BigDecimal (coerce->big-decimal [v _] (bigdec v)))

(extend-type java.lang.Long
  Coerce->String     (coerce->string      [v _] (str v))
  Coerce->Integer    (coerce->integer     [v _] v)
  Coerce->BigInteger (coerce->big-integer [v _] (bigint v))
  ;;
  Coerce->Decimal    (coerce->decimal     [v _] (float  v))
  Coerce->BigDecimal (coerce->big-decimal [v _] (bigdec v)))

;;;

(extend-type java.math.BigInteger
  Coerce->String     (coerce->string      [v _] (str v))
  Coerce->Integer    (coerce->integer     [v _] (int v))
  Coerce->BigInteger (coerce->big-integer [v _] v)
  ;;
  Coerce->Decimal    (coerce->decimal     [v _] (float  v))
  Coerce->BigDecimal (coerce->big-decimal [v _] (bigdec v)))

;;;

(extend-type java.lang.Double
  Coerce->String     (coerce->string      [v _] (str v))
  Coerce->Integer    (coerce->integer     [v _] (int v))
  Coerce->BigInteger (coerce->big-integer [v _] (bigint v))

  ;; Note 'upgrades' it to a float for consistency as that is our
  ;; default for parsing.
  Coerce->Decimal    (coerce->decimal        [v _] (float  v))
  Coerce->BigDecimal (coerce->big-decimal    [v _] (bigdec v)))

(extend-type java.lang.Float
  ;; Same as for java.lang.Double
  Coerce->String     (coerce->string      [v _] (str v))
  Coerce->Integer    (coerce->integer     [v _] (int v))
  Coerce->BigInteger (coerce->big-integer [v _] (bigint v))
  ;;
  Coerce->Decimal    (coerce->decimal     [v _] v)
  Coerce->BigDecimal (coerce->big-decimal [v _] (bigdec v)))

;;;

(extend-type clojure.lang.Keyword
  Coerce->String  (coerce->string  [v _] (name v))
  Coerce->Keyword (coerce->keyword [v _] v))

(extend-type java.util.UUID
  Coerce->String (coerce->string [v _] (str v)))

;;;
