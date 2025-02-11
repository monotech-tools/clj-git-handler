
(ns git-handler.submodule-updater.core.side-effects
    (:require [git-handler.submodule-updater.builder.side-effects  :as submodule-updater.builder.side-effects]
              [git-handler.submodule-updater.detector.side-effects :as submodule-updater.detector.side-effects]
              [git-handler.submodule-updater.updater.side-effects  :as submodule-updater.updater.side-effects]
              [git-handler.submodule-updater.print.side-effects    :as submodule-updater.print.side-effects]
              [common-state.api :as common-state]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn update-submodule-dependencies!
  ; @important
  ; This function operates only in Clojure projects that use 'deps.edn' file to manage dependencies!
  ;
  ; @note
  ; This function updates dependencies (in 'deps.edn' files) that are referenced in the following format:
  ; {:deps {author/my-repository {:git/url "..." :sha "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"}}}
  ;
  ; @description
  ; - Pushes commits of changed submodules, and updates the 'deps.edn' file in other submodules with the returned commit SHA.
  ; - This function updates dependencies (in 'deps.edn' files) that are referenced in the following format:
  ;   {:deps {author/my-repository {:git/url "..." :sha "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"}}}
  ;
  ; @param (map)(opt) options
  ; {:config (map)(opt)
  ;   {"author/my-repository" (map)(opt)
  ;     {:commit-message-f (function)(opt)
  ;      :on-pushed-f (function)(opt)
  ;      :target-branch (string)(opt)}}
  ;    :default (map)(opt)
  ;     {:commit-message-f (function)(opt)
  ;       Default time.api/timestamp-string
  ;      :on-pushed-f (function)(opt)
  ;      :target-branch (string)(opt)
  ;       Default: "main"}}
  ;  :source-paths (vector)(opt)
  ;   Default: ["submodules"]}
  ;
  ; @usage
  ; (update-submodule-dependencies!)
  ;
  ; @usage
  ; (update-submodule-dependencies! {:source-paths ["my-submodules"]})
  ;
  ; @usage
  ; (defn my-commit-message-f [previous-commit-message] ...)
  ; (defn my-on-pushed-f      [submodule-path pushed-commit-message pushed-commit-sha] ...)
  ; (update-submodule-dependencies! {:config {:default {:commit-message-f my-commit-message-f
  ;                                                     :on-pushed-f      my-on-pushed-f
  ;                                                     :target-branch    "my-branch"}})
  ;
  ; @usage
  ; (defn my-commit-message-f [previous-commit-message] ...)
  ; (defn my-on-pushed-f      [submodule-path pushed-commit-message pushed-commit-sha] ...)
  ; (update-submodule-dependencies! {:config {"author/my-repository" {:commit-message-f my-commit-message-f
  ;                                                                   :on-pushed-f      my-on-pushed-f
  ;                                                                   :target-branch    "my-branch"}}})
  ([]
   (update-submodule-dependencies! {}))

  ([options]
   (try (do (common-state/dissoc-state! :git-handler :submodule-updater)
            (submodule-updater.detector.side-effects/detect-submodules!             options)
            (submodule-updater.detector.side-effects/detect-submodule-dependencies! options)
            (submodule-updater.builder.side-effects/build-dependency-cascade!       options)
            (submodule-updater.builder.side-effects/build-dependency-tree!          options)
            (submodule-updater.print.side-effects/print-dependency-tree!            options)
            (submodule-updater.updater.side-effects/update-submodules!              options))
        (catch Exception e (println e)))))
