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
package io.wcm.caravan.jaxrs.publisher.sampleservice2;

import io.wcm.caravan.jaxrs.publisher.JaxRsComponent;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

/**
 * Sample JAX-RS Service
 */
@Component(immediate = true)
@Service(JaxRsComponent.class)
@Path("/")
public class JaxRsService implements JaxRsComponent {

  /**
   * Sample method
   */
  @GET
  @Path("sample")
  public String getSample() {
    return "Sample #2";
  }

}
