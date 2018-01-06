(ns frontend.core
    (:require
      [cljs-http.client :as http]
      [cljs.core.async :refer [<! go]]
      [clojure.string :as str]
      [reagent.core :as r]))

;; -------------------------
;; Helper Functions

(defn remove-annotations-from-album-title
  [album-title]
  (str/trim
    (str/replace album-title
                 #"(\(.*\)|\[.*\])" "")))

(defn parse-album-data
  [album-data]
  (let [album-name (remove-annotations-from-album-title (:album-name album-data))
        image-url (->> album-data
                       :images
                       (apply (partial max-key :width)) ; get largest image
                       :url)]
    (assoc album-data :album-name album-name
                      :image-url image-url)))


(defn remove-preceding-articles [string]
  (-> string
    (str/trim)
    (str/replace #"(?i)^(the|a|an) " "")
    (str/trim)))

(defn sort-key-fn [sort-key]
  "Wow this needs a better name"
  (fn [x]
    (-> x
        sort-key
        remove-preceding-articles
        str/upper-case)))

(defn sort-group [sort-fn [group-key group]]
  [group-key (sort-by sort-fn group)])

(defn get-album-groups [grouping sorting albums]
  "Wow this function looks horrendous"
  (let [sort-fn (case grouping
                  :alphabet (sort-key-fn sorting)
                  :artist (sort-key-fn :album-name))]
    (->>
      (case grouping
        :alphabet (group-by (comp first remove-preceding-articles sorting) albums)
        :artist (group-by :artist-name albums))
      (vec)
      (sort-by (sort-key-fn first))
      (mapv (partial sort-group sort-fn)))))

;; -------------------------
;; State

(defonce albums (r/atom nil))

(defonce sorting (r/atom :artist-name))

(defonce grouping (r/atom :none))

;; -------------------------
;; Views

(defn album-display [album-data]
  (let [{:keys [album-name album-url artist-name artist-url image-url]} album-data]
    [:div.card
     [:img.card-img-top.img-fluid {:src image-url}]
     [:div.card-block
       [:h5.card-title.text-truncate
        [:a {:href album-url
             :title album-name}
         album-name]]
       [:h6.text-truncate
        [:a {:href artist-url
             :title artist-name}
         artist-name]]]]))

(defn album-list [albums-data]
  [:div.row
   (for [album albums-data]
     ^{:key album} [:div.col-md-3.col-sm-5 [album-display album]])])

(defn album-groups [groupings]
  [:div
    (for [[group-key group] groupings]
      ^{:key group-key}
      [:div
       [:div.row
        [:h2.group-title group-key]]
       [album-list group]])])

(defn exclusive-button-group-button [button-title button-state state-atom]
  (let [checked (= button-state @state-atom)]
    [:label.btn.btn-secondary
     {:class (if checked
               "active"
               "")
      :on-click #(reset! state-atom button-state)}
     [:input {:type "radio"
              :checked checked
              :auto-complete "off"}]
     button-title]))

(defn exclusive-button-group [state-atom & buttons]
  [:div.btn-group.btn-group-toggle {:data-toggle "buttons"}
    (for [[title state] buttons]
      ^{:key state} [exclusive-button-group-button title state state-atom])])

(defn sorting-buttons [sorting-atom]
  [:div.row.control-buttons
    [:label.btn.col-1 [:strong "Sort by:"]]
    [exclusive-button-group sorting-atom
     ["Title" :album-name]
     ["Artist" :artist-name]]])

(defn grouping-buttons [grouping-atom]
  [:div.row.control-buttons
   [:label.btn.col-1 [:strong "Group by:"]]
   [exclusive-button-group grouping-atom
    ["None" :none]
    ["Alphabet" :alphabet]
    ["Artist" :artist]]])

(defn app-entry []
  [:div.container-fluid
   [:h1 "Thomas's Favorite Albums"]
   [sorting-buttons sorting]
   [grouping-buttons grouping]
   (if (= :none @grouping)
     [album-list (sort-by (sort-key-fn @sorting) @albums)]
     [album-groups (get-album-groups @grouping @sorting @albums)])
   [:p "View the code on "
      [:a {:href "https://github.com/tanelso2/favorite-albums-webapp"}
       "GitHub"]]])

;; -------------------------
;; Initialize app

(defn mount-root []
  (r/render [app-entry] (.getElementById js/document "app")))

(defn fetch-albums []
  (go (let [response (<! (http/get "data/albums.json"))
            body (:body response)
            album-data (mapv parse-album-data body)]
        (println "Fetched data successfully!")
        (reset! albums album-data))))

(defn init! []
  (fetch-albums)
  (mount-root))
