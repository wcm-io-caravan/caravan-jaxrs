/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2019 wcm.io
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

import java.util.Set;

import org.osgi.service.component.annotations.Component;

import io.wcm.caravan.jaxrs.publisher.JaxRsClassesProvider;

/**
 * Sample JAX-RS classes provider.
 */
@Component(service = JaxRsClassesProvider.class, immediate = true)
public class JaxRsClassesProviderService implements JaxRsClassesProvider {

  @Override
  public Set<Class<?>> getClasses() {
    return Set.of(RequestScopeDateResource.class);
  }

}
