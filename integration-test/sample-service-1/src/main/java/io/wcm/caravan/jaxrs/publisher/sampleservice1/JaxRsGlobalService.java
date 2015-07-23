/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2015 wcm.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.wcm.caravan.jaxrs.publisher.sampleservice1;

import io.wcm.caravan.jaxrs.publisher.ApplicationPath;
import io.wcm.caravan.jaxrs.publisher.JaxRsComponent;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

/**
 * Sample JAX-RS Service that is registered to all JAX-RS appliations in the OSGi container.
 */
@Component
@Service(value = JaxRsComponent.class, serviceFactory = true)
@Property(name = JaxRsComponent.PROPERTY_GLOBAL_COMPONENT, value = "true")
@Path("/globalServiceId")
public class JaxRsGlobalService implements JaxRsComponent {

  private String serviceId;

  @Activate
  protected void activate(ComponentContext componentContext) {
    serviceId = ApplicationPath.get(componentContext);
  }

  /**
   * Returns service id detected from OSGi component context
   */
  @GET
  @Produces("text/plain")
  public String getServiceId() {
    return serviceId;
  }

}
