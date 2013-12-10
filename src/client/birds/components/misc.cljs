(ns birds.components.misc
  (:require [pump :as pump]
            [react :as react]
            [clojure.string :as string]
            [birds.shared.util :refer [scale-image]]
            [birds.util :refer [log pluralize pretty-date]]
            [birds.dom :refer [kill-event select-contents radio-group-value] :as dom]
            [dommy.core :refer [listen! attr set-text! value] :as dommy]
            [cljs.core.async :refer [chan]])
  (:require-macros [pump.macros :refer [defr component] :as pump-macros]                   
                   [dommy.macros :refer [sel sel1]]))

(defn handle-editable-keydown
  [e]
  (when (= 13 (.-which e))
    (.preventDefault e)
    (.blur (.-target e))))

(defr EditableText
  [c props s]
  [:span {:contentEditable true
          :class-name "editable"
          :title (or (:title props) "Click to edit")
          :on-mouse-up #(select-contents (.-target %))
          :on-key-down handle-editable-keydown
          :on-blur #((:fn props) (string/trim (.-innerText (.-target %))) %)}
   (:val props)])

(defr FileUploadButton
  :handle-upload (fn [c p s e]
                   (let [file (aget (.-files (.-target e)) 0)]
                     (if (.match (.-type file) "image.*")
                       (let [reader (js/FileReader.)]
                         (set! (.-onload reader)
                               (fn [e & rest]
                                 (log "e & rest" e rest)
                                 ((:callback p) (.-result (.-target e)))))
                         (.readAsDataURL reader file)))))
  [c p s]
  [:div.file-upload.btn.btn-block.btn-default
   [:label {:for "file-upload"} "Upload an Image"]
   [:input#file-upload {:type "file"
                        :on-change (.-handleUpload c)}]])

(defr DialogButton [c p s]
  [:button {:class-name (string/join " " (conj ["btn"] (:class-name p)))
            :on-click (:on-click p)}
   (:text p)])

(defr DialogCancelButton [c p s]
  [DialogButton {:class-name "default"
           :on-click (fn [e]
                       (.preventDefault e)
                       ((:close p)))
           :text (or (:text p) "Cancel")}])

(defr Dialog
  [c props s]  
  [:div.modal.backdrop
   [:div.modal-dialog
    [:div.modal-content
     (when-let [header (:header props)]
       [:div.modal-header [header props]])
     (when-let [body (:body props)]
       [:div.modal-body [body props]])
     (when-let [footer (:footer props)]
       [:div.modal-footer [footer props]])]]])

(defn handle-image-change [props new-src]
  (log "hanlde img chnage" props new-src)
  ((:update props) new-src))

(defr ImageDialogBody
  :handle-change (fn [p new-src] ((:update p) new-src))
  [c p s]
  (log "c" c)
  [:div.row
   [:div.col-lg-6
    [:div.image-dialog__image
     [:img {:src (scale-image (:src p) 244)}]]]
   [:div.col-lg-6
    #_[:button.btn.btn-block.btn-primary "Use My Gravatar"]
    #_[:button.btn.btn-block.btn-default "Use My Account Image"]
    [FileUploadButton (assoc p :callback (fn [src] (handle-image-change p src)))]]])

(defr ImageDialogFooter
  :handle-save (fn [c p s]
                 ((:save p) (:src p))
                 ((:close p)))
  [c p s] 
  [:div
   [:button.btn.btn-primary {:on-click (.-handleSave c)} "Save"]
   [:button.btn.btn-default {:on-click (:close p)} "Cancel"]])


(defr ImageDialog [c p s]
  [Dialog (merge p {:body ImageDialogBody
                  :footer ImageDialogFooter})])


