# Documentation for the Blogging Platform

## Components

The blogging platform is composed of the following components:

* [Web Server](components/web-server.md): Sets up the HTTP server and provides basic functionality, such as cookies, headers and security measurements.
* [Theme](components/theme.md): Styles the responses with a consistent theme.
* [Authentication](components/auth.md): If enabled, enforces that users are properly authenticated.
* [Aritcle](components/article.md): Handles the functionality that is related to article display and management.
* [Comment](components/comment.md): Handles the functionality that is related to comment display and management.

## Plugins

The article text can be further processed by the following plugins:

* [Dropbox Images](plugins/dropbox.md): Enables a shortcut syntax to include images from a public Dropbox folder.
* [Google Maps](plugins/google-map.md): Enables a shortcut syntax to include Google Maps with your markers.
* [Smilies](plugins/smiley.md): Replaces punctuation smileys with icons.
* [Escape HTML](plugins/escape-html.md): Escapes security-relevant HTML tags from the text. Usually used for user-provided comments.
* [Markdown](plugins/markdown.md): Transforms an article provided in Markdown syntax to HTML.

## Deployment

The blogging system can be deployed on different environments. A few examples are given below:

* [Your own server](deployment/own-server.md)
* [Openshift](deployment/openshift.md): A cloud provider with an extensive free tier.
* [Heroku](deployment/heroku.md): Another cloud provider.

## Security

The security measurements are further discussed in [security.md](security.md).
