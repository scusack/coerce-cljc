(ns coerce.utils
  (:require [clojure.string :as s]))

(defn clean-string
  [v]
  (when-not (s/blank? v)
    (s/trim v)))

(defn remove-junk-from-integer
  [s]
  (s/replace s #"[^0-9]" ""))

(defn collapse-spaces
  [s]
  (s/replace s #"\s+" " "))

(defn keywordify
  [s]
  (some-> s
          clean-string
          collapse-spaces
          s/lower-case
          (s/replace #"\W" "-")
          (s/replace #"[-]{2,}" "-")
          keyword))

(defn guess-sign
  [s]
  ;; We look for the first - sign from the beginning followed by a
  ;; number, if we find we are negative otherwise we aren't.
  ;;
  ;; We ignore whitespace between the - and the digit.
  (if (re-find #"^[^0-9]*-[\s]*[0-9]" s)
    "-" ""))

(defn extract-leading-integer
  [s]
  (some->> (s/split s #"\.")
           first
           remove-junk-from-integer
           clean-string
           (str (guess-sign s))))

(defn extract-leading-decimal
  [s]
  (let [[i1 i2] (some->> (s/split s #"\.")
                         (take 2)
                         (map remove-junk-from-integer)
                         (map clean-string))]
    (when i1 (str (guess-sign s) i1 (when i2 ".") i2))))

(defn uuid-string?
  [s]
  (re-find #"(?i)^[\da-f]{8}-[\da-f]{4}-[\da-f]{4}-[\da-f]{4}-[\da-f]{12}$" s))
