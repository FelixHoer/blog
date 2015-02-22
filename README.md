# Blog

A modular blogging platform written in Clojure. The main goal was to provide different modules and datastores that can be mixed and matched, to satisfy needs of the blog operator (I want comments or authentication), as well as to comply with the capabilities of the server (Is a database available?). It features:

* **Articles**
  * Stored in the filesystem: Articles are read from a folder. New articles are composed on the author's machine and are then transfered on the server.
  * Stored in a database: The articles are composed in the web application and stored in a serverside SQL database.

* **Comments** per article (optional)
  * Stored in a database: Comments are stored in a serverside SQL database.

* **Authentication** (optional)
  * Stored in the filesystem: The user information is stored in a file. To add a new user the file is modified and those changes have to be transfered to the server.
  * Stored in a database: The user information is stored in a SQL database.

A more detailed documentation can be found in the [doc folder](doc).

## Example Configurations

* **Simple Public Blog** running on a cloud provider without a database: 
  * Articles are read from a folder on the server. New articles are composed locally and pushed to the server via git (which is available on most cloud services).
  * No comments are needed.
  * No authentication is needed, as the blog should be public.

* **Secure Private Blog** running on a server with a database: 
  * Authentication is needed, as the blog should only be accessible to certain people. The users are stored in the database and can be managed via the web interface.
  * Articles are read from the database. New articles are added via the web interface. This allows blogging from mobile devices that can't easily push files to the server.
  * Comments are also stored in the database.
  * HTTPS will be enforced. For cloud providers this usually involves dealing with a reverse proxy.

## Usage

* Configure the system: Enable and configure the components in `src/blog/core.clj`.
* Adjust the template: Change the labels and appearance of the pages in `resources/templates`.
* (If used) Set up the database.
* Start the application: run `lein run` locally or push the repository to your cloud provider.

## License

* Smiley Icons: Public Domain, by [Tango Project](http://tango.freedesktop.org/Tango_Icon_Library)
* Source Code: Eclipse Public License
