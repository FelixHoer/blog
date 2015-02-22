# Web-Server-Component

This component starts a web server with middleware that provides basic functionality, such as request parameters, session, encoding. The server will then dispatch the requests to the connected `Handler` components.

## Configuration

Modify your `core.clj` as follows:

```
(def CSP {:default-src ["'self'"]
          :img-src     ["*"]
          :script-src  ["https://ajax.googleapis.com"
                        "https://oss.maxcdn.com"
                        "https://netdna.bootstrapcdn.com"]
          :style-src   ["'self'"
                        "https://netdna.bootstrapcdn.com"]
          :font-src    ["https://netdna.bootstrapcdn.com"]
          :frame-src   ["https://www.google.com/maps/embed/"]})

(def web-server (map->WebServer {:port 8080
                                 :csp CSP
                                 :ssl {:via-reverse-proxy? true}
                                 :handlers [:theme-handler
                                            :auth-handler
                                            :article-management-handler
                                            :article-handler
                                            :comment-management-handler
                                            :comment-handler]}))

(def system
  (component/system-map
   :web-server (component/using web-server
                                {:theme-handler              :theme-handler
                                 :auth-handler               :auth-handler
                                 :article-management-handler :article-management-handler
                                 :article-handler            :article-handler
                                 :comment-management-handler :comment-management-handler
                                 :comment-handler            :comment-handler})
   ...))
```

The options for the `WebServer` are:
* `:port`: An integer that specifies on which port the server should listen for requests.
* `:csp`: A map that specifies valid origins of resources. The keys are described in the Content Security Policy.
* `:ssl`: If provided, HTTPS will be enforced on all request (redirects for HTTP). There are two kinds of maps expected, depending if the HTTPS is provided by a reverse proxy, or if the web server has to provide HTTPS connections.
  * If a reverse proxy takes care of HTTPS connections, as it is the case for most cloud providers:
    * `:via-reverse-proxy`: Set to true to indicate this mode of operation.
  * If the web server has to provide HTTPS connections:
    * `:keystore`: Path to a Java Keystore file.
    * `:key-password`: Password to access the Keystore file.
    * `:ssl-port`: An integer that specifies the port for the ssl/https connections.
* `:handlers`: A vector of keys from the system-map. This vector defines in which order the handlers (components) should be executed.

## Using a SSL-certificate

Create a self-signed certificate and add it to a keystore:

```
openssl genrsa -des3 -out jetty.key
openssl req -new -x509 -key jetty.key -out jetty.crt
openssl pkcs12 -inkey jetty.key -in jetty.crt -export -out jetty.pkcs12
$JAVA_HOME/bin/keytool -importkeystore -srckeystore jetty.pkcs12 -srcstoretype PKCS12 -destkeystore keystore
```

Your `core.clj` might look as follows:

```
(def web-server (map->WebServer {..
                                 :ssl {:keystore "/tmp/keystore"
                                       :key-password "jettypass"
                                       :ssl-port 8443}
                                 ...}))
```