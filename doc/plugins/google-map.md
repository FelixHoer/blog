# Google Maps Plugin

Google's popular Map service is also available for external sites.
This pulgin uses the [Google Maps Embed API](https://developers.google.com/maps/documentation/embed/guide) to embed maps of places or routes into your articles. To do this, the syntax of Markdown's image tags has been enhanced:

```
![<title>](map:<waypoint-1>|...|<waypoint-n>)
```

## Example (of a place)

```
![Capital of Austria][map:Vienna+Austria]
```

## Example (of a route)

```
![Tour: South to North][map:Klagenfurt+Austria|Graz+Austria|Vienna+Austria]
```

## Configuration

This plugin requires an application key form Google for the site, where the map should be embedded (so the URL of your blog). 

To get the application key:

1. visit the APIs Console: https://code.google.com/apis/console
2. log into your Google account
3. click Services
4. activate "Google Maps Embed API"
5. click "API Access link"
6. click "Create new Browser key..."
7. insert as referer your blog's URL, for example: "localhost:8080/*"
8. you will now see the application key

The application key has to be provided in `core.clj`-file:

```
(def google-map-plugin (map->GoogleMapPlugin {:app-key "GOOGLE_MAP_APP_KEY"}))

(def article-handler (map->ArticleHandler {:plugins [:google-map-plugin
                                                     ...]}))

(def system
  (component/system-map
   ...
   :google-map-plugin google-map-plugin
   ...
   :article-handler (component/using article-handler
                                     {...
                                      :google-map-plugin :google-map-plugin
                                      ...})))
```