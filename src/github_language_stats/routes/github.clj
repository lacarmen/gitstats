(ns github-language-stats.routes.github
  (:require [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :refer :all]
            [tentacles.repos :as r]
            [environ.core :refer [env]]))

(def auth (env :github-key))

(defn get-user-repos [user]
  (loop [page 1
         repos []]
    (let [result (r/user-repos user {:page page :per-page 100 :auth auth})]
      (if (= 100 (count result))
        (recur (inc page)
               (into repos result))
        (into repos result)))))

(defn get-user-owned-repos [user]
  (let [repos (get-user-repos user)]
    (->> repos
         (filter #(not (:fork %)))
         (map :name))))

(defn get-language-stats [user]
  (let [repos (get-user-owned-repos user)]
    (try (->> repos
              (map #(r/languages user % :auth auth))
              (apply merge-with +)
              ok)
         (catch Exception e
           (not-found (str "Failed to find GitHub user with name " user))))))

(defroutes github-routes
           (GET "/language-stats" [user]
             (get-language-stats user)))
