(ns github-language-stats.app
  (:require [github-language-stats.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
