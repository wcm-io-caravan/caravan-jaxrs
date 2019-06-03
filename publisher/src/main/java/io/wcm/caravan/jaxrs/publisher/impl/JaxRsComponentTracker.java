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
package io.wcm.caravan.jaxrs.publisher.impl;

import static io.wcm.caravan.jaxrs.publisher.impl.ServletContainerBridge.isJaxRsGlobal;

import javax.ws.rs.Path;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import io.wcm.caravan.jaxrs.publisher.JaxRsComponent;

/**
 * Tracks {@link JaxRsComponent} instances.
 */
class JaxRsComponentTracker extends ServiceTracker<JaxRsComponent, Object> {

  private final ServletContainerBridge bridge;
  private final BundleContext bundleContext;
  private final Bundle bundle;

  JaxRsComponentTracker(ServletContainerBridge bridge) {
    super(bridge.getBundleContext(), JaxRsComponent.class, null);
    this.bridge = bridge;
    this.bundleContext = bridge.getBundleContext();
    this.bundle = bridge.getBundle();
  }

  @Override
  public Object addingService(ServiceReference<JaxRsComponent> reference) {
    if (isJaxRsGlobal(reference)) {
      JaxRsComponent serviceInstance = bundle.getBundleContext().getService(reference);
      if (isJaxRsComponent(serviceInstance)) {
        ServletContainerBridge.log.debug("Register global component {} for {}", serviceInstance.getClass().getName(), bundle.getSymbolicName());
        bridge.getGlobalComponents().add(serviceInstance);
        bridge.markAsDirty();
      }
    }
    else if (reference.getBundle() == bundle) {
      JaxRsComponent serviceInstance = bundle.getBundleContext().getService(reference);
      if (isJaxRsComponent(serviceInstance)) {
        ServletContainerBridge.log.debug("Register component {} for {}", serviceInstance.getClass().getName(), bundle.getSymbolicName());
        bridge.getLocalComponents().add(serviceInstance);
        bridge.markAsDirty();
      }
    }
    return super.addingService(reference);
  }

  @Override
  public void removedService(ServiceReference<JaxRsComponent> reference, Object service) {
    if (isJaxRsGlobal(reference)) {
      JaxRsComponent serviceInstance = bundle.getBundleContext().getService(reference);
      if (isJaxRsComponent(serviceInstance)) {
        ServletContainerBridge.log.debug("Unregister global component {} for {}", serviceInstance.getClass().getName(), bundle.getSymbolicName());
        bridge.getGlobalComponents().remove(serviceInstance);
        bundleContext.ungetService(reference);
        bridge.markAsDirty();
      }
    }
    else if (reference.getBundle() == bundle) {
      JaxRsComponent serviceInstance = bundle.getBundleContext().getService(reference);
      if (isJaxRsComponent(serviceInstance)) {
        ServletContainerBridge.log.debug("Unregister component {} for {}", serviceInstance.getClass().getName(), bundle.getSymbolicName());
        bridge.getLocalComponents().remove(serviceInstance);
        bundleContext.ungetService(reference);
        bridge.markAsDirty();
      }
    }
    super.removedService(reference, service);
  }

  private boolean isJaxRsComponent(Object serviceInstance) {
    return (serviceInstance instanceof JaxRsComponent && hasAnyJaxRsAnnotation(serviceInstance.getClass()));
  }

  private boolean hasAnyJaxRsAnnotation(Class<?> clazz) {
    return clazz.isAnnotationPresent(Path.class)
        || clazz.isAnnotationPresent(Provider.class)
        || clazz.isAnnotationPresent(PreMatching.class);
  }

}
