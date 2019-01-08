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
package io.wcm.caravan.jaxrs.publisher;

import java.util.Collection;
import java.util.Collections;

/**
 * Marker interface that have to implement all JAX-RS services and providers.
 */
//CHECKSTYLE:OFF
public interface JaxRsComponent {
  //CHECKSTYLE:ON

  /**
   * OSGi property name that marks JaxRS components to be registered globally to all JAX-RS applications.
   * The property value has to be set to "true".
   * Such components have to be marked as "serviceFactory" as well to ensure bundle scope.
   */
  String PROPERTY_GLOBAL_COMPONENT = "caravan.jaxrs.global";

  /**
   * Can be implemented by any JaxRsComponent to register additional root resource, provider and
   * {@link javax.ws.rs.core.Feature} classes to the JAX-RS runtime. These classes do not have to implement
   * {@link JaxRsComponent} and should not be OSGI components (as their lifecycle and dependency injection is completely
   * managed by JAX-RS). This allows to implement request-scoped resources with constructor initialization. Since OSGI's
   * dependency injection is not possible for these instances, other singleton {@link JaxRsComponent} dependencies
   * can be injected with the {@link javax.ws.rs.core.Context} annotation instead.
   * @return a collection of classes to be returned by JAX-RS {@link javax.ws.rs.core.Application#getClasses()}
   */
  default Collection<Class<?>> getAdditionalJaxRsClassesToRegister() {
    return Collections.emptyList();
  }
}
