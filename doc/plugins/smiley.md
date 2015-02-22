# Smiley-Plugin

This plugin turns textual smilies into smiley-images.

## Example

For example, the textual smiley:

```
:-)
```

Will become:

```
<img src="/smiley/face-smile.png" />
```

## Full list of smilies

* `:angel:` `0:)` `O:)` `0:-)` `O:-)`
* `:cry:` `:'(`
* `:devil:` `>:)`
* `:glasses:` `B)` `B-)`
* `:grin:` `:D` `:-D`
* `:kiss:` `:*` `:-*`
* `:plain:` `:|` `:/` `:-|` `:-/`
* `:smile:` `:)` `:-)`
* `:surprise:` `:o` `:O` `:-o` `:-O`
* `:wink:` `;)` `;-)`
* `:love:` `<3`

## Configuration

Modify `core.clj` as follows:

```
(def smiley-plugin (map->SmileyPlugin {}))

(def article-handler (map->ArticleHandler {:plugins [:smiley-plugin
                                                     ...]}))

(def system
  (component/system-map
   ...
   :smiley-plugin smiley-plugin
   ...
   :article-handler (component/using article-handler
                                     {...
                                      :smiley-plugin :smiley-plugin
                                      ...})))
```

## License

The Smiley Icons are provided by the [Tango Project](http://tango.freedesktop.org/Tango_Icon_Library), which released them into Public Domain.