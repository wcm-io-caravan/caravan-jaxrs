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
package io.wcm.caravan.jaxrs.publisher.impl;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ServerProperties;

import com.google.common.collect.Sets;

import io.wcm.caravan.jaxrs.publisher.JaxRsClassesProvider;
import io.wcm.caravan.jaxrs.publisher.JaxRsComponent;

/**
 * Minimal JAX-RS application registering all JAX-RS OSGi components as singletons.
 */
class JaxRsApplication extends Application {

  private static final Map<String, Object> DEFAULT_PROPERTIES = Map.of(
      // look for implementations described by META-INF/services/*
      ServerProperties.METAINF_SERVICES_LOOKUP_DISABLE, false,
      // disable auto discovery on server, as it's handled via OSGI
      ServerProperties.FEATURE_AUTO_DISCOVERY_DISABLE, true,
      // disable WADL generation by default
      ServerProperties.WADL_FEATURE_DISABLE, true);

  private final Set<JaxRsComponent> localComponents;
  private final Set<JaxRsComponent> globalComponents;
  private Set<JaxRsClassesProvider> localClassesProviders;
  private Set<JaxRsClassesProvider> globalClassesProviders;

  JaxRsApplication(Set<JaxRsComponent> localComponents, Set<JaxRsComponent> globalComponents,
      Set<JaxRsClassesProvider> localClassesProviders, Set<JaxRsClassesProvider> globalClassesProviders) {
    this.localComponents = localComponents;
    this.globalComponents = globalComponents;
    this.localClassesProviders = localClassesProviders;
    this.globalClassesProviders = globalClassesProviders;
  }

  @Override
  public Set<Object> getSingletons() {
    return Sets.union(globalComponents, localComponents);
  }

  @Override
  public Set<Class<?>> getClasses() {
    return Stream.concat(localClassesProviders.stream(), globalClassesProviders.stream())
        .flatMap(component -> component.getClasses().stream())
        .collect(Collectors.toSet());
  }

  @Override
  public Map<String, Object> getProperties() {
    return DEFAULT_PROPERTIES;
  }

}
