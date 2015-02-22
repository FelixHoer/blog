# Security

## Password Storage

Independent of the storage method, the passwords themselves are never stored on the server. From the passwords a key is derived via `scrypt`. The parameters for this key derivation function are chosen in a way that it takes around 200ms to perform it on an `AWS t1.micro server`. So even if an attacker gets access to the key's, it would be infeasible to bruteforce the passwords from them.

## HTTPS

If the authentication component is used, HTTPS should be enforced. This can be configured in the `web-server` component. Cloud providers, such as Heroku and Openshift, usually provide HTTPS via a reverse proxy. Otherwise on your own server, you can provide certificates via the Java Keystore to the webserver.

## Cross-Site Scripting (XSS)

There are two measurements in place against XSS:
* Input from untrusted sources is escaped by the templating engine or in the case of comments by the `escape-html` plugin.
* Content Security Policy (CSP) header is set, which limits the valid sources of resources.

## Cross-Site Request Forgery (CSRF)

During the processing of each request, an anti CSRF token is generated. If the next request is sent as POST, this token has to be provided to ensure that the origin and the time-frame is valid.

## HTTP Session Security

The session cookie is secured with the following measurements:
* The `http-only` flag is set, therefore, the session value can't be read by Javascript or Flash.
* If https is used, the `secure` flag is set. Therefore, the cookie will only be sent over https connection and not be exposed in unencrypted communication.

## Anti Framing Headers

A header (`X-Frame-Options: DENY`) is sent to tell browsers that they shouldn't allow to frame this page, such as including it via an iframe into another page. Otherwise an attacker might use this method to perform clickjacking.

## Anti Content Type Sniffing Headers

A response header (`X-Content-Type-Options: nosniff`) is set that tells browsers to use a `text\plain` content type if no other is provided via the response headers. If this header would not be set, and no content type is provided, the browser might try to figure out the content type by itself. This could lead to a mistakenly assumed content type, which provides an attacker with attack vectors.

## HTTP Strict Transport Security (HSTS)

A header is set that tells the browser to only use HTTPS for this domain in the future. This is a measurement against SSL-stripping attacks, where a man-in-the-middle attacker replaces HTTPS- with HTTP-links to still be able to read the then unencrypted traffic. With the HSTS header set, would not succeed, as the browser remembers that HTTP connections to the requested domain are invalid. See [HSTS](http://en.wikipedia.org/wiki/HTTP_Strict_Transport_Security) on Wikipedia.

## SQL-Injection

Since only prepared statements are used, the SQL statements can't be manipulated later on.

## Path-Traversal Attacks

The articles may be loaded from the filesystem and in this process the input of the HTTP request is also included. An attacker might try to craft a request to get access to unauthorized parts of the filesystem. This is mitigated by making sure that every path built from untrusted input is still in the designated area.
