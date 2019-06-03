AEM Caravan JAX-RS Publisher Sample
===================================

This is an example AEM project with a simple JAX-RS services published via Caravan JAX-RS Publisher.


### Build and deploy

To build the application run

```
mvn clean install
```

To build and deploy the application to your local AEM instance use these scripts:

* `build-deploy.sh` - Build and deploy to author instances
* `build-deploy-publish.sh` - Build and deploy to publish instances

After deployment you can open the URL with the REST Service in your browser:

* Author: http://localhost:4502/aem-caravan-jaxrs-sample/hello
* Publish: http://localhost:4503/aem-caravan-jaxrs-sample/hello


### System requirements

* JDK 1.8
* Apache Maven 3.3.9 or higher
* AEM 6.3 author instance running on port 4502
* Optional: AEM 6.3 publish instance running on port 4503

It is recommended to set up the local AEM instances with `nosamplecontent` run mode.
