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

import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.wcm.caravan.jaxrs.publisher.ApplicationPath;

/**
 * Bundle tracker that listens for all bundles added to the OSGi systems, and searches for JAX-RS components.
 * If any is found and an application path is defined a jersey instance dedicated to this bundle is instantiated.
 */
@Component(immediate = true)
public class JaxRsBundleTracker implements BundleTrackerCustomizer<ComponentInstance> {

  private static final Logger log = LoggerFactory.getLogger(JaxRsBundleTracker.class);

  private BundleTracker<ComponentInstance> bundleTracker;

  @Reference(target = "(" + ComponentConstants.COMPONENT_FACTORY + "=" + ServletContainerBridge.SERVLETCONTAINER_BRIDGE_FACTORY + ")")
  private ComponentFactory servletContainerBridgeFactory;

  @Activate
  void activate(ComponentContext componentContext) {
    BundleContext bundleContext = componentContext.getBundleContext();
    this.bundleTracker = new BundleTracker<ComponentInstance>(bundleContext, Bundle.ACTIVE, this);
    this.bundleTracker.open();
  }

  @Deactivate
  void deactivate() {
    this.bundleTracker.close();
  }

  @Override
  public ComponentInstance addingBundle(Bundle bundle, BundleEvent event) {
    String applicationPath = ApplicationPath.get(bundle);
    if (StringUtils.isNotBlank(applicationPath)) {

      if (log.isInfoEnabled()) {
        log.info("Mount JAX-RS application {} to {}", bundle.getSymbolicName(), applicationPath);
      }

      // register JAX-RS application as servlet on HTTP whiteboard
      Dictionary<String, Object> serviceConfig = new Hashtable<>();
      serviceConfig.put("alias", applicationPath);
      serviceConfig.put(ServletContainerBridge.PROPERTY_BUNDLE_ID, bundle.getBundleId());
      return servletContainerBridgeFactory.newInstance(serviceConfig);
    }
    return null;
  }

  @Override
  public void modifiedBundle(Bundle bundle, BundleEvent event, ComponentInstance componentInstance) {
    // nothing to do
  }

  @Override
  public void removedBundle(Bundle bundle, BundleEvent event, ComponentInstance componentInstance) {
    if (componentInstance == null) {
      return;
    }
    if (log.isInfoEnabled()) {
      String applicationPath = ApplicationPath.get(bundle);
      log.info("Unmount JAX-RS application {} from {}", bundle.getSymbolicName(), applicationPath);
    }
    componentInstance.dispose();
  }

}
