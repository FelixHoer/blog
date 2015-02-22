# Markdown-Plugin

This plugin will convert your Markdown articles into HTML. 
To do so it uses `markdown-clj` internally. 
For a Syntax-Guide to Markdown see the page of [Daring Fireball](http://daringfireball.net/projects/markdown/syntax).

## Configuration

Modify `core.clj` as follows:

```
(def markdown-plugin (map->MarkdownPlugin {}))

(def article-handler (map->ArticleHandler {:plugins [...
                                                     :markdown-plugin]}))

(def system
  (component/system-map
   ...
   :markdown-plugin markdown-plugin
   ...
   :article-handler (component/using article-handler
                                     {...
                                      :markdown-plugin   :markdown-plugin})))
```