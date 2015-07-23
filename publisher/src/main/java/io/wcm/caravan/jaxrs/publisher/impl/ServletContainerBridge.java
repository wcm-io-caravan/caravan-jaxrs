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

import io.wcm.caravan.commons.stream.Streams;
import io.wcm.caravan.jaxrs.publisher.JaxRsComponent;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.ws.rs.Path;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Service;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * Servlet bridge that wraps a Jersy JAX-RS ServletContainer and ensures the JAX-RS context is reloaded
 * when service registrations (local or global) change.
 * It automatically registers JAX-RS components from the current bundle and instances
 * from global JAX-RS components factories.
 */
@Component(factory = ServletContainerBridge.SERVLETCONTAINER_BRIDGE_FACTORY)
@Service(Servlet.class)
public class ServletContainerBridge extends HttpServlet {
  private static final long serialVersionUID = 1L;

  static final String SERVLETCONTAINER_BRIDGE_FACTORY = "caravan.jaxrs.servletcontainer.bridge.factory";
  static final String PROPERTY_BUNDLE = "caravan.jaxrs.relatedBundle";

  private BundleContext bundleContext;
  private Bundle bundle;
  private JaxRsApplication application;
  private ServletContainer servletContainer;
  private volatile boolean isDirty;
  private Set<JaxRsComponent> localComponents;
  private Set<JaxRsComponent> globalComponents;
  private ServiceTracker localComponentTracker;
  private Collection<ServiceReference<JaxRsComponent>> globalJaxRSComponentReferences;

  private static final Logger log = LoggerFactory.getLogger(ServletContainerBridge.class);

  @Activate
  void activate(ComponentContext componentContext) {
    // bundle which contains the JAX-RS services
    bundle = (Bundle)componentContext.getProperties().get(PROPERTY_BUNDLE);
    bundleContext = bundle.getBundleContext();

    // initialize component tracker to detect non-global JAX-RS components in current bundle
    localComponents = Sets.newConcurrentHashSet();
    globalComponents = Sets.newConcurrentHashSet();
    localComponentTracker = new JaxRsComponentTracker();
    localComponentTracker.open();

    // initialize JAX-RS application and Jersey Servlet container
    application = new JaxRsApplication(localComponents, globalComponents);
    servletContainer = new ServletContainer(ResourceConfig.forApplication(application));
  }

  @Deactivate
  void deactivate(ComponentContext componentContext) {
    if (localComponentTracker != null) {
      localComponentTracker.close();
    }
    if (globalJaxRSComponentReferences != null) {
      Streams.of(globalJaxRSComponentReferences).forEach(bundleContext::ungetService);
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
    servletContainer.init();
  }

  @Override
  public void init(ServletConfig config) throws ServletException {
    servletContainer.init(config);
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


  /**
   * Tracks JAX-RS components in the current bundle.
   */
  private class JaxRsComponentTracker extends ServiceTracker<JaxRsComponent, Object> {

    public JaxRsComponentTracker() {
      super(bundleContext, JaxRsComponent.class, null);
    }

    @Override
    public Object addingService(ServiceReference<JaxRsComponent> reference) {
      if (isJaxRsGlobal(reference)) {
        JaxRsComponent serviceInstance = bundle.getBundleContext().getService(reference);
        if (isJaxRsComponent(serviceInstance)) {
          globalComponents.add(serviceInstance);
          log.debug("Registered global component {} for {}", serviceInstance.getClass().getName(), bundle.getSymbolicName());
          isDirty = true;
        }
      }
      else if (reference.getBundle() == bundle) {
        JaxRsComponent serviceInstance = bundle.getBundleContext().getService(reference);
        if (isJaxRsComponent(serviceInstance)) {
          localComponents.add(serviceInstance);
          log.debug("Registered component {} for {}", serviceInstance.getClass().getName(), bundle.getSymbolicName());
          isDirty = true;
        }
      }
      return super.addingService(reference);
    }

    @Override
    public void removedService(ServiceReference<JaxRsComponent> reference, Object service) {
      if (isJaxRsGlobal(reference)) {
        JaxRsComponent serviceInstance = bundle.getBundleContext().getService(reference);
        if (isJaxRsComponent(serviceInstance)) {
          globalComponents.remove(serviceInstance);
          bundleContext.ungetService(reference);
          log.debug("Unregistered global component {} for {}", serviceInstance.getClass().getName(), bundle.getSymbolicName());
          isDirty = true;
        }
      }
      else if (reference.getBundle() == bundle) {
        JaxRsComponent serviceInstance = bundle.getBundleContext().getService(reference);
        if (isJaxRsComponent(serviceInstance)) {
          localComponents.remove(serviceInstance);
          bundleContext.ungetService(reference);
          log.debug("Unregistered component {} for {}", serviceInstance.getClass().getName(), bundle.getSymbolicName());
          isDirty = true;
        }
      }
      super.removedService(reference, service);
    }

    private boolean isJaxRsGlobal(ServiceReference<JaxRsComponent> serviceReference) {
      return "true".equals(serviceReference.getProperty(JaxRsComponent.PROPERTY_GLOBAL_COMPONENT))
          && Constants.SCOPE_BUNDLE.equals(serviceReference.getProperty(Constants.SERVICE_SCOPE));
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


}
