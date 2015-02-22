# Deployment on Your Own Server

1. Download the Repository: `git clone https://github.com/FelixHoer/blog.git`
2. Configure the system: Enable and configure the components in `src/blog/core.clj`.
3. Adjust the template: Change the labels and appearance of the pages in `resources/templates`.
4. If required by any components, set up and start the database.
5. Start the application: run `lein run` locally or push the repository to your cloud provider. This requires [Leiningen](http://leiningen.org/).