(ns github-language-stats.routes.home
  (:require [github-language-stats.layout :as layout]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :refer [ok]]))

(defn home-page []
  (layout/render "home.html"))

(defroutes home-routes
           (GET "/" [] (home-page)))
