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

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.wcm.caravan.jaxrs.publisher.JaxRsClassesProvider;
import io.wcm.caravan.jaxrs.publisher.JaxRsComponent;

/**
 * Servlet bridge that wraps a Jersy JAX-RS ServletContainer and ensures the JAX-RS context is reloaded
 * when service registrations (local or global) change.
 * It automatically registers JAX-RS components from the current bundle and instances
 * from global JAX-RS components factories.
 */
@Component(service = Servlet.class, factory = ServletContainerBridge.SERVLETCONTAINER_BRIDGE_FACTORY)
public class ServletContainerBridge extends HttpServlet {
  private static final long serialVersionUID = 1L;

  static final String SERVLETCONTAINER_BRIDGE_FACTORY = "caravan.jaxrs.servletcontainer.bridge.factory";
  static final String PROPERTY_BUNDLE_ID = "caravan.jaxrs.relatedBundleId";

  private BundleContext bundleContext;
  private Bundle bundle;
  private JaxRsApplication application;
  private ServletContainer servletContainer;
  private volatile boolean isDirty;
  private Set<JaxRsComponent> localComponents;
  private Set<JaxRsComponent> globalComponents;
  private ServiceTracker<JaxRsComponent, Object> jaxRsComponentTracker;
  private Set<JaxRsClassesProvider> localClassesProviders;
  private Set<JaxRsClassesProvider> globalClassesProviders;
  private ServiceTracker<JaxRsClassesProvider, Object> jaxClassesProviderTracker;

  static final Logger log = LoggerFactory.getLogger(ServletContainerBridge.class);

  @Activate
  void activate(ComponentContext componentContext) {
    // bundle which contains the JAX-RS services
    bundle = componentContext.getBundleContext().getBundle(
        (Long)componentContext.getProperties().get(PROPERTY_BUNDLE_ID));
    bundleContext = bundle.getBundleContext();

    // initialize component tracker to detect local and global JAX-RS components for current bundle
    localComponents = new ConcurrentSkipListSet<>();
    globalComponents = new ConcurrentSkipListSet<>();
    jaxRsComponentTracker = new JaxRsComponentTracker(this);
    jaxRsComponentTracker.open();

    // initialize component tracker to detect local and global additionall JAX-RS classes
    localClassesProviders = new ConcurrentSkipListSet<>();
    globalClassesProviders = new ConcurrentSkipListSet<>();
    jaxClassesProviderTracker = new JaxRsClassesProviderTracker(this);
    jaxClassesProviderTracker.open();
  }

  @Deactivate
  void deactivate() {
    if (jaxRsComponentTracker != null) {
      jaxRsComponentTracker.close();
    }
    if (jaxClassesProviderTracker != null) {
      jaxClassesProviderTracker.close();
    }
  }

  @Override
  public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
    reloadIfDirty();
    // delegate all calls to jersey servlet container
    servletContainer.service(request, response);
  }

  @Override
  public void init() throws ServletException {
    // initialize JAX-RS application and Jersey Servlet container
    application = new JaxRsApplication(localComponents, globalComponents,
        localClassesProviders, globalClassesProviders);
    servletContainer = new ServletContainer(ResourceConfig.forApplication(application));
    servletContainer.init(getServletConfig());
  }

  @Override
  public void destroy() {
    servletContainer.destroy();
  }

  /**
   * Checks if components where added or removed.
   * If yes the jersey servlet container is reloaded with the new configuration, blocking all other calls in the
   * meantime.
   */
  private void reloadIfDirty() {
    if (isDirty) {
      synchronized (this) {
        if (isDirty) {
          // reload configuration if any service requests have changed
          log.debug("Reload JAX-RS servlet container of {}", bundle.getSymbolicName());
          servletContainer.reload(ResourceConfig.forApplication(application));
          isDirty = false;
        }
      }
    }
  }

  @Override
  public String toString() {
    return "jaxrs-servlet:" + bundle.getSymbolicName();
  }

  BundleContext getBundleContext() {
    return this.bundleContext;
  }

  Bundle getBundle() {
    return this.bundle;
  }

  Set<JaxRsComponent> getLocalComponents() {
    return this.localComponents;
  }

  Set<JaxRsComponent> getGlobalComponents() {
    return this.globalComponents;
  }

  Set<JaxRsClassesProvider> getLocalClassesProviders() {
    return this.localClassesProviders;
  }

  Set<JaxRsClassesProvider> getGlobalClassesProviders() {
    return this.globalClassesProviders;
  }

  void markAsDirty() {
    this.isDirty = true;
  }

  static boolean isJaxRsGlobal(ServiceReference<?> serviceReference) {
    return "true".equals(serviceReference.getProperty(JaxRsComponent.PROPERTY_GLOBAL_COMPONENT))
        && Constants.SCOPE_BUNDLE.equals(serviceReference.getProperty(Constants.SERVICE_SCOPE));
  }

}
