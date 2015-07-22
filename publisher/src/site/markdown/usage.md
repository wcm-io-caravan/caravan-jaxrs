## JAX-RS Publisher Usage

### Configure Application Path

To enable the JAX-RS integration for your OSGi bundle add a new instruction `Caravan-JaxRs-ApplicationPath` to the OSGi manifest. The best way for this is using the maven-bundle-plugin. Example:

```xml
<build>
  <plugins>

    <plugin>
      <groupId>org.apache.felix</groupId>
      <artifactId>maven-bundle-plugin</artifactId>
      <configuration>
        <instructions>
          <Caravan-JaxRs-ApplicationPath>/service/myJaxRsService</Caravan-JaxRs-ApplicationPath>
        </instructions>
      </configuration>
    </plugin>

  </plugins>
</build>
```


### Register JAX-RS component

If you want to register a JAX-RS component to the JAX-RS application created for the bundle you have to define it as OSGi Service implementing the interface `io.wcm.caravan.commons.jaxrs.JaxRsComponent`. Example:

```java
@Component(immediate = true)
@Service(JaxRsComponent.class)
@Path("/{tenantId}/index")
public class HalEntryPoint implements JaxRsComponent {

  @GET
  public Response index(@PathParam("tenantId") String tenantId) {
    // your code...
  }

}
```

The application path must not to be added to the `@Path` annotation, it is added automatically. This example services can be reached via the URL `/service/myJaxRsService/tenant123/index`.


### Register global JAX-RS components

If you want to register a JAX-RS component for all JAX-RS application in the OSGi instance and not only for the current bundle you can define a OSGi factory service with the factory name `caravan.jaxrs.global.factory`. Example:

```java
@Component(factory = JaxRsComponent.GLOBAL_COMPONENT_FACTORY)
@Service(JaxRsComponent.class)
@Provider
public class StatusCodeAwareExceptionMapper implements ExceptionMapper<RuntimeException>, JaxRsComponent {

  @Override
  public Response toResponse(RuntimeException ex) {
    // your code...
  }

}

```
