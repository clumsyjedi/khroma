(ns khroma.context-menus
  (:require [cljs.core.async :as async])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))


(defn create [create-properties callback]
  (js/chrome.contextMenus.create (clj->js create-properties) callback))

(defn clicks []
  (let [ch (async/chan)]
    (js/chrome.contextMenus.onClicked.addListener 
      (fn [info tab]
        (go (async/>! ch {:info (js->clj info) :tab tab}))))
    ch))
