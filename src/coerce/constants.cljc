(ns coerce.constants)

(def +date-patterns+
  {:yyyy-mm-dd          "yyyy-MM-dd"
   :yyyy-mm-dd-HHmm     "yyyy-MM-dd HH:mm"
   :dd-mm-yyyy          "dd-MM-yyyy"
   :dd-mm-yyyy-hhmma    "dd-MM-yyyy hh:mma"
   :dd|mm|yyyy          "dd/MM/yyyy"
   :dd|mm|yyyy-hhmma    "dd/MM/yyyy hh:mma"
   :day-dd|mm|yyyy      "EEEE, dd/MM/yyyy"
   :mm-dd-yyyy          "MM-dd-yyyy"
   :mm|dd|yyyy          "MM/dd/yyyy"
   :day-mm|dd|yyyy      "EEEE, MM/dd/yyyy"
   :rfc-3999            "yyyy-MM-dd'T'HH:mm:ssXXX"
   :iso-8601            "yyyy-MM-dd'T'kk:mm:ssZ"
   :yyyy-mm-dd-hh-mm-ss "yyyy-MM-dd kk:mm:ss"
   :week                "ww"
   :day                 "EEEE"})
