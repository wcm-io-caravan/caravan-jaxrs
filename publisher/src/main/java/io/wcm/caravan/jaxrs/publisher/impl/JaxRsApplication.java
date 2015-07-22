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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ServerProperties;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * Minimal JAX-RS application registering all JAX-RS OSGi components as singletons.
 */
class JaxRsApplication extends Application {

  private static final Map<String, Object> DEFAULT_PROPERTIES = ImmutableMap.<String, Object>builder()
      // look for implementations described by META-INF/services/*
      .put(ServerProperties.METAINF_SERVICES_LOOKUP_DISABLE, false)
      // disable auto discovery on server, as it's handled via OSGI
      .put(ServerProperties.FEATURE_AUTO_DISCOVERY_DISABLE, true)
      // disable WADL generation by default
      .put(ServerProperties.WADL_FEATURE_DISABLE, true)
      .build();

  private final Set<Object> localComponents;
  private final Collection<Object> globalComponents;

  public JaxRsApplication(Set<Object> localComponents, Collection<Object> globalComponents) {
    this.localComponents = localComponents;
    this.globalComponents = globalComponents;
  }

  @Override
  public Set<Object> getSingletons() {
    return Sets.union(ImmutableSet.copyOf(globalComponents), localComponents);
  }

  @Override
  public Map<String, Object> getProperties() {
    return DEFAULT_PROPERTIES;
  }

}
