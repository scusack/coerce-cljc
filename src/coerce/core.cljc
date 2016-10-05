(ns coerce.core
  (:require [clojure.string :as s]
            [coerce.protocols :refer [coerce->string
                                      coerce->boolean
                                      coerce->date
                                      coerce->integer
                                      coerce->big-integer
                                      coerce->decimal
                                      coerce->big-decimal
                                      coerce->keyword
                                      coerce->uuid]]
            [coerce.utils :refer [clean-string]]
            [coerce.impl :as impl]))

;;; --------------------------------------------------------------------------------
;;  Something like this is clojure

(defmulti coerce (fn [v to & {:as options}]
                   (if (or (nil? v) (and (string? v) (s/blank? v)))
                     ::nil
                     to)))

(defmethod coerce ::nil
  [v _ & _]
  nil)

(defmethod coerce :string
  [v _ & options]
  (coerce->string v options))

(defmethod coerce :boolean
  [v _ & options]
  (coerce->boolean v options))

(defmethod coerce :date
  [v _ & options]
  (coerce->date v options))

(defmethod coerce :integer
  [v _ & options]
  (coerce->integer v options))

(defmethod coerce :big-integer
  [v _ & options]
  (coerce->big-integer v options))

(defmethod coerce :decimal
  [v _ & options]
  (coerce->decimal v options))

(defmethod coerce :big-decimal
  [v _ & options]
  (coerce->big-decimal v options))

(defmethod coerce :keyword
  [v _ & options]
  (coerce->keyword v options))

(defmethod coerce :uuid
  [v _ & options]
  (coerce->uuid v options))

(defmethod coerce :url-encoded
  [v _ & options]
  (some-> v clean-string impl/url-encode))

;;; --------------------------------------------------------------------------------
