(ns witan.gateway.schema-test
  (:require [clojure.test :refer :all]
            [witan.gateway.schema :refer :all]))

(deftest semvar-test
  (is (semver? "1.0.0"))
  (is (semver? "1.2.3"))
  (is (not (semver? "1.2.")))
  (is (not (semver? "1.2")))
  (is (not (semver? "1.2")))
  (is (not (semver? "1.")))
  (is (not (semver? "1")))
  (is (not (semver? "")))
  (is (not (semver? "foobar1.2.3")))
  (is (not (semver? "foobar1.2.3foobar")))
  (is (not (semver? "1.2.3foobar")))
  (is (not (semver? "v1.2.3")))
  (is (not (semver? "V1.2.3")))
  (is (not (semver? "1.2.3v")))
  (is (not (semver? "1.2.3V"))))
