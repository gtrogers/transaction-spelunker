(ns transaction-spelunker.html_generators
  (:require [hiccup.core]
            [hiccup.page]))

(defn table [headers columns]
  (hiccup.core/html [:table [:thead [:tr (for [heading headers] [:th heading])]]
                     [:tbody (for [col columns]
                               [:tr [:th (first col)]
                                (for [c (rest col)] [:td c])])]]))
