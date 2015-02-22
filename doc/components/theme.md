# Theme-Component

Renders the data, which was composed by the previous components, by using [Selmer](https://github.com/yogthos/Selmer) and the templates in the `resources/templates/` folder.

## Configuration

Modify your `core.clj` as follows:

```
(def theme-handler (map->ThemeHandler {:template-resource-path "templates"
                                       :static-resource-path "static"
                                       :static-data {:active-components #{:auth
                                                                          :article
                                                                          :article-management
                                                                          :comment
                                                                          :comment-management}}}))

(def system
  (component/system-map
   :web-server (component/using web-server
                                {:theme-handler :theme-handler
                                 ...})
   ...                                 
   :theme-handler theme-handler
   ...))
```

The options for the `WebServer` are:
* ':template-resource-path': This path specifies where the template files of the theme can be found.
* ':static-resource-path': Resources under this path will be served without any further processing, such as checking for authentication.
* ':static-data': A map that is provided to the templating system. It can be used to tweak the output of the templates. The default theme uses the following key:
  * ':active-components': A set of keywords that specifies which components are empty. The default templates use this information to, for example, switch off the comment section output, if the comment component is disabled.
