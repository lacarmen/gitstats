(ns github-language-stats.home
  (:require [reagent.core :refer [atom]]
            [reagent.session :as session]
            [ajax.core :refer [GET POST]]
            [goog.string :as gstring]
            [goog.string.format]))

(defn input-val [input]
  (-> input .-target .-value))

(defn get-language-stats [user stats error preloader-hidden?]
  (GET "/language-stats"
       {:params        {:user user}
        :handler       #(do (reset! preloader-hidden? true)
                            (reset! stats %))
        :error-handler #(do (reset! preloader-hidden? true)
                            (reset! stats nil)
                            (reset! error (:response %)))}))

(defn search [user stats error preloader-hidden?]
  (when (not-empty user)
    (reset! error nil)
    (reset! stats nil)
    (reset! preloader-hidden? false)
    (get-language-stats user stats error preloader-hidden?)))

(defn total [stats]
  (->> stats
       (map second)
       (reduce +)))

(defn percentages [stats]
  (reduce
    (fn [m [k v]]
      (merge m {k (->> (/ v (total stats))
                       (* 100)
                       (gstring/format "%.1f"))}))
    {}
    stats))

(defn make-table [stats]
  (reduce (fn [rows [lang percent]]
            (into rows [[:tr [:td (name lang)] [:td percent]]]))
          [:tbody]
          stats))

(defn home-page []
  (let [user (atom "")
        repo-name (atom "")
        stats (atom nil)
        preloader-hidden? (atom true)
        error (atom nil)]
    (fn []
      [:div.container
       [:div.row
        [:div.input-field.col.m10.offset-m1.s12
         [:div.card-panel
          [:div.row
           [:div.col.m10.offset-m1
            [:h4 "GitHub User Language Statistics"]

            [:input {:type        "text"
                     :placeholder "GitHub user"
                     :value       @user
                     :on-change   #(reset! user (input-val %))}]
            [:input {:type        "text"
                     :placeholder "Repository Name"
                     :value       @repo-name
                     :on-change   #(reset! repo-name (input-val %))}]

            [:a.btn.teal.lighten-2
             {:on-click #(search @user stats error preloader-hidden?)}
             "Search"]

            [:br]

            [:div {:hidden @preloader-hidden?}
             [:h5.center-align "Searching..."]
             [:div.progress
              [:div.indeterminate]]]

            (when @error
              [:div.row
               [:div.col.s12
                [:div.card-panel.red.lighten-2
                 [:p.error.center-align @error]]]])

            (when @stats
              [:table.bordered
               [:thead
                [:tr
                 [:th "Language"] [:th "Percentage"]]]
               (make-table (percentages @stats))])]]]]]])))