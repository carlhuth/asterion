(ns draft.core
  (:require [clojure.string :as str]
            [cljs.nodejs :as node]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [draft.tree :as tree]
            cljsjs.d3))
            

(enable-console-print!)

(defonce app-state (atom {:ns ""
                          :highlight ""
                          :files []}))

(def fs (node/require "fs"))

(def d3 (.-d3 js/window))

(defn draw! [data]
  (.json d3 "projects/draft.json"
    (fn [json]
      (tree/drawTree "#graph"
        (clj->js
          {:ns (str/split (:ns data) " ")
           :highlight (str/split (:highlight data) " ")})
        json))))

(defn main [data owner]
  (reify
    om/IRender
    (render [_]
      (dom/div nil
        (dom/span #js {:className "controls"}
          "filter ns: "
          (dom/input #js {:type "text"
                          :value (:ns data)
                          :onKeyDown (fn [e]
                                       (when (= "Enter" (.-key e))
                                         (draw! data)))
                          :onChange (fn [e]
                                      (om/update! data :ns
                                        (.. e -target -value)))}))
        (dom/br #js {})
        (dom/span #js {:className "controls"}
          "highlight ns: "
          (dom/input #js {:type "text"
                          :value (:highlight data)
                          :onKeyDown (fn [e]
                                       (when (= "Enter" (.-key e))
                                         (draw! data)))
                          :onChange (fn [e]
                                      (om/update! data :highlight
                                        (.. e -target -value)))}))
        (dom/svg #js {:id "graph"}
          (dom/g #js {}))))
    om/IDidMount
    (did-mount [_]
      (draw! data))))

(defn main-page [data owner]
  (om/component
    (dom/div nil
      (if (empty? (:files data))
        (dom/h1 nil "Loading...")
        (apply dom/ul nil
          (map (partial dom/li nil) (:files data)))))))

(defn mount! []
  (om/root main app-state {:target  (.getElementById js/document "app")}))

(fs.readdir "/"
  (fn [error files]
    (swap! app-state assoc :files (js->clj files))
    (mount!)))
