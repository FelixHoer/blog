# Deployment on OpenShift

OpenShift is a Plattform as a Service (PaaS) Provider, which offers in their free-tier:

* three virtual machines (small Gears), which will go into idle-mode after two days without a request.
* these Gears can be used to power a webserver and your databases.

## Set up openshift

1. Prerequisite: install `ruby` and `rubygems`
2. Install the console application: `sudo gem install rhc`
3. Set up your account: `rhc setup`

## Deploy the blog on OpenShift

1. Download the Repository: `git clone https://github.com/FelixHoer/blog.git`
2. Add the openshift repository as a remote: `git remote add openshift -f <openshift-git-repo-url>` (you got this url from `rhc setup`)
2. Configure the blog by setting up `core.clj`.
3. If you are using the ArticleFileDatastore:
   1. Add your articles, in the articles folder.
   2. Commit your changes: `git commit`
4. If you are using the AuthFileDatastore:
   1. Add users to the users-file as described in the documentation for the authentication component.
   2. Commit your changes: `git commit`
5. Push changes to openshift, which will restart your server: `git push openshift master`
6. Check the deployment logs: `rhc tail blog`
