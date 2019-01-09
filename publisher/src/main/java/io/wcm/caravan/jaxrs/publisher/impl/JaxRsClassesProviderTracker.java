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

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import io.wcm.caravan.jaxrs.publisher.JaxRsClassesProvider;

/**
 * Tracks {@link JaxRsClassesProvider} instances.
 */
class JaxRsClassesProviderTracker extends ServiceTracker<JaxRsClassesProvider, Object> {

  private final ServletContainerBridge bridge;
  private final BundleContext bundleContext;
  private final Bundle bundle;

  JaxRsClassesProviderTracker(ServletContainerBridge bridge) {
    super(bridge.getBundleContext(), JaxRsClassesProvider.class, null);
    this.bridge = bridge;
    this.bundleContext = bridge.getBundleContext();
    this.bundle = bridge.getBundle();
  }

  @Override
  public Object addingService(ServiceReference<JaxRsClassesProvider> reference) {
    if (isJaxRsGlobal(reference)) {
      JaxRsClassesProvider serviceInstance = bundle.getBundleContext().getService(reference);
      ServletContainerBridge.log.debug("Register global classes provider {} for {}", serviceInstance.getClass().getName(), bundle.getSymbolicName());
      bridge.getGlobalClassesProviders().add(serviceInstance);
      bridge.markAsDirty();
    }
    else if (reference.getBundle() == bundle) {
      JaxRsClassesProvider serviceInstance = bundle.getBundleContext().getService(reference);
      ServletContainerBridge.log.debug("Register classes provider {} for {}", serviceInstance.getClass().getName(), bundle.getSymbolicName());
      bridge.getLocalClassesProviders().add(serviceInstance);
      bridge.markAsDirty();
    }
    return super.addingService(reference);
  }

  @Override
  public void removedService(ServiceReference<JaxRsClassesProvider> reference, Object service) {
    if (isJaxRsGlobal(reference)) {
      JaxRsClassesProvider serviceInstance = bundle.getBundleContext().getService(reference);
      ServletContainerBridge.log.debug("Unregister global classes provider {} for {}", serviceInstance.getClass().getName(), bundle.getSymbolicName());
      bridge.getGlobalClassesProviders().remove(serviceInstance);
      bundleContext.ungetService(reference);
      bridge.markAsDirty();
    }
    else if (reference.getBundle() == bundle) {
      JaxRsClassesProvider serviceInstance = bundle.getBundleContext().getService(reference);
      ServletContainerBridge.log.debug("Unregister classes provider {} for {}", serviceInstance.getClass().getName(), bundle.getSymbolicName());
      bridge.getLocalClassesProviders().remove(serviceInstance);
      bundleContext.ungetService(reference);
      bridge.markAsDirty();
    }
    super.removedService(reference, service);
  }

}
