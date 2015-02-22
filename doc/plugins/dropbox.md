# Dropbox-Plugin

The cloud storage provider [Dropbox](https://www.dropbox.com) offers it's users free storage space and a public folder, which can be accessed and linked without restriction.

This plugin provides a convenient way to link images, which are placed into the public folder, in your articles. To do this, the Dropbox-Plugin turns Markdown's image tags, which are annotated with the `db:` URL-prefix, to the author's public folder on Dropbox.

## Example

For example, the following image tag:

```
![Sunrise over the Beach](db:holiday/sunrise.jpg)
```

Will be replaced by: 

```
![Sunrise over the Beach](http://dl.dropboxusercontent.com/u/<Dropox-User-Id>/holiday/sunrise.jpg)
```

## Configuration

This plugin requires the author's user id on Dropbox. It has to be provided in `core.clj`-file:

```
(def dropbox-plugin (map->DropboxPlugin {:user-id "DROPBOX_USER_ID"}))

(def article-handler (map->ArticleHandler {:plugins [:dropbox-plugin
                                                     ...]}))

(def system
  (component/system-map
   ...
   :dropbox-plugin     dropbox-plugin
   ...
   :article-handler (component/using article-handler
                                     {...
                                      :dropbox-plugin :dropbox-plugin
                                      ...})))
```