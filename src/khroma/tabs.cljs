(ns khroma.tabs
  (:require [khroma.log :as console]
            [khroma.messaging :refer [channel-from-port chan] :as messaging]
            [khroma.util :refer [options->jsparams]]
            [clojure.walk :as walk]
            [cljs.core.async :as async])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(defn get-tab [tab-id]
  (let [ch (async/chan)]
    (.get js/chrome.tabs tab-id
      (fn [tab]
        (async/put! ch (walk/keywordize-keys (js->clj {:tab tab}))))) ch))

(defn get-active-tab []
  (let [ch (async/chan)]
    (.query js/chrome.tabs #js {:active true :currentWindow true}
      (fn [result]
        (when-let [tab (first result)]
          (async/put! ch (walk/keywordize-keys (js->clj {:tab tab})))))) ch))

(defn- tab-action-events [instance & key-args]
  (let [ch (async/chan)]
      (.addListener instance
        (fn [& val-args]
          (async/put! ch (walk/keywordize-keys (js->clj (zipmap key-args val-args)))))) ch))

(defn tab-updated-events []
  (tab-action-events js/chrome.tabs.onUpdated :tabId :changeInfo :tab))

(defn tab-removed-events []
  (tab-action-events js/chrome.tabs.onRemoved :tabId :removeInfo))

(defn tab-replaced-events []
  (tab-action-events js/chrome.tabs.onReplaced :added :removed))

(defn connect [& {:keys [tabId connectInfo]}]
  (channel-from-port
    (.apply     
      js/chrome.tabs.connect js/chrome.tabs
      (options->jsparams [tabId connectInfo]))))

(defn send-message [tabId message responseCallback]
  (.sendMessage js/chrome.tabs tabId (clj->js message) responseCallback))


