# Escape-HTML-Plugin

This plugin will escape potentually harmful HTML tags from the text. This can be useful if text from an untrusted source, such as comments, should be embedded into the page.

## Configuration

Modify `core.clj` as follows:

```
(def escape-html-plugin (map->EscapeHTMLPlugin {:preserve-ampersand? true}))

(def comment-handler (map->CommentHandler {:plugins [:escape-html-plugin
                                                     ...]}))

(def system
  (component/system-map
   ...
   :escape-html-plugin escape-html-plugin
   ...
   :comment-handler (component/using comment-handler
                                     {...
                                      :escape-html-plugin :escape-html-plugin
                                      ...})))
```