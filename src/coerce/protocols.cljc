(ns coerce.protocols)

;;; --------------------------------------------------------------------------------
;;  Protocols for all the basic types, v = value, o = options.

(defprotocol Coerce->String
  (coerce->string [v o]))

(defprotocol Coerce->Boolean
  (coerce->boolean [v o]))

(defprotocol Coerce->Date
  (coerce->date [v o]))

(defprotocol Coerce->Integer
  (coerce->integer [v o]))

(defprotocol Coerce->BigInteger
  (coerce->big-integer [v o]))

(defprotocol Coerce->Decimal
  (coerce->decimal [v o]))

(defprotocol Coerce->BigDecimal
  (coerce->big-decimal [v o]))

(defprotocol Coerce->Keyword
  (coerce->keyword [v o]))

(defprotocol Coerce->UUID
  (coerce->uuid [v o]))
