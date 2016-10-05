(ns coerce.core-test
  #?(:clj  (:require [clojure.test :refer [deftest is testing run-tests]]
                     [coerce.core :refer [coerce]])
     :cljs (:require [cljs.test   :refer-macros [deftest is testing run-tests]]
                     [coerce.core :refer        [coerce]])))


(comment
  ;; For running in a clojurescript repl.  There must be an easier way?

  (require '[cljs.repl :as repl])
  (require '[cljs.repl.rhino :as rhino]) ;; require the rhino implementation of IJavaScriptEnv
  ;; (def env (rhino/repl-env))             ;; create a new environment
  (repl/repl (rhino/repl-env))
  (require 'coerce.core-test :reload)
  (in-ns 'coerce.core-test)
  (run-tests))

(deftest coercions-to-nil
  (testing "Blank Strings"

    (is (-> (coerce "\t \t  " :string) nil?))

    (is (-> (coerce "" :string)      nil?))
    (is (-> (coerce "" :boolean)     nil?))
    (is (-> (coerce "" :integer)     nil?))
    (is (-> (coerce "" :big-integer) nil?))
    (is (-> (coerce "" :date)        nil?))
    (is (-> (coerce "" :uuid)        nil?))
    ;;
    (is (-> (coerce "  " :string)      nil?))
    (is (-> (coerce "  " :boolean)     nil?))
    (is (-> (coerce "  " :integer)     nil?))
    (is (-> (coerce "  " :big-integer) nil?))
    (is (-> (coerce "  " :date)        nil?))))

(deftest boolean-coercions
  (testing "Strings to Bools"

    (is (-> (coerce "t" :boolean) true?))
    (is (-> (coerce "f" :boolean) false?))
    ;;
    (is (-> (coerce "y"   :boolean) true?))
    (is (-> (coerce "ye"  :boolean) true?))
    (is (-> (coerce "yes" :boolean) true?))
    ;;
    (is (-> (coerce "n"  :boolean) false?))
    (is (-> (coerce "no" :boolean) false?))
    ;;
    ;; Watch out this returns TRUE
    (is (-> (coerce "not" :boolean) true?))))

(deftest number-coercions
  (testing "Strings To Number values"

    (is (-> (coerce "42" :integer)     (= 42)))
    (is (-> (coerce "42" :big-integer) (= 42)))
    ;;
    (is (-> (coerce "42.42" :integer)     (= 42)))
    (is (-> (coerce "42.42" :big-integer) (= 42)))
    ;;
    #?(:clj (do
              (is (-> (coerce "42.42" :integer     :strict? true) nil?))
              (is (-> (coerce "42.42" :big-integer :strict? true) nil?))
              ;;
              (is (thrown? NumberFormatException (coerce "42.42" :integer     :strict? true :throw? true)))
              (is (thrown? NumberFormatException (coerce "42.42" :big-integer :strict? true :throw? true))))
       :cljs (do
               (is (-> (coerce "42.42" :integer     :strict? true) (= 42)))
               (is (-> (coerce "42.42" :big-integer :strict? true) (= 42)))
               ;;
               ;; FIXME: How does exception handling work in javascript?
               #_(is (thrown? NumberFormatException (coerce "42.42" :integer     :strict? true :throw? true)))
               #_(is (thrown? NumberFormatException (coerce "42.42" :big-integer :strict? true :throw? true)))))))

(deftest date-coercions
  (testing "Dates to Strings"
    (is (= (coerce #inst "2016-10-10" :string) "2016-10-10"))
    (is (= (coerce #inst "2016-10-10" :string :pattern :dd-mm-yyyy) "10-10-2016"))
    (is (= (coerce #inst "2016-10-10" :string :pattern :dd|mm|yyyy) "10/10/2016"))
    ;;
    (comment
     (is (= (coerce "2016-10-10" :date)                      #inst "2016-10-10"))
     (is (= (coerce "10-10-2016" :date :pattern :dd-mm-yyyy) #inst "2016-10-10"))
     (is (= (coerce "10/10/2016" :date :pattern :dd|mm|yyyy) #inst "2016-10-10")))))

(deftest keyword-coercions
  (testing "Strings to keywords"
    (is (= (coerce "this-is-a-test" :keyword) :this-is-a-test))
    (is (= (coerce "This-Is-A-Test" :keyword :strict? true) :This-Is-A-Test))
    ;;
    (is (= (coerce "This is a Test"          :keyword) :this-is-a-test))
    (is (= (coerce "This   \tIs A  \t  Test" :keyword) :this-is-a-test))))

(deftest uuid-coercions
  (testing "Strings to UUIDs"
    (is (= (coerce "cf20e1dc-861a-11e6-ae22-56b6b6499611" :uuid) #uuid "cf20e1dc-861a-11e6-ae22-56b6b6499611"))
    (is (= (coerce "CF20E1DC-861A-11E6-AE22-56B6B6499611" :uuid) #uuid "cf20e1dc-861a-11e6-ae22-56b6b6499611"))
    ;;
    (is (nil? (coerce "cf20e1dc861a11e6ae2256b6b6499611" :uuid)))
    (is (nil? (coerce "cf20e1dc--56b6b6499611"           :uuid)))

    (is (= (coerce #uuid "cf20e1dc-861a-11e6-ae22-56b6b6499611" :string) "cf20e1dc-861a-11e6-ae22-56b6b6499611"))
    (is (= (coerce #uuid "CF20E1DC-861A-11E6-AE22-56B6B6499611" :string) "cf20e1dc-861a-11e6-ae22-56b6b6499611"))

    ;;
    #?(:clj (do
              (is (thrown? IllegalArgumentException (coerce "cf20e1dc861a11e6ae2256b6b6499611" :uuid :throw? true)))
              (is (thrown? IllegalArgumentException (coerce "cf20e1dc--56b6b6499611"           :uuid :throw? true)))))))
