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

import io.wcm.caravan.jaxrs.publisher.ApplicationPath;
import io.wcm.caravan.jaxrs.publisher.JaxRsComponent;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;
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
  static final String PROPERTY_GLOBAL_COMPONENT = "caravan.jaxrs.globalComponent";

  private BundleContext bundleContext;
  private Bundle bundle;
  private String applicationPath;
  private JaxRsApplication application;
  private ServletContainer servletContainer;
  private volatile boolean isDirty;
  private Set<Object> localComponents;
  private ServiceTracker localComponentTracker;

  // collect all JAX-RS components from all bundles that are marked as "global"
  @Reference(name = "globalJaxRsComponentFactory", referenceInterface = ComponentFactory.class,
      target = "(" + ComponentConstants.COMPONENT_FACTORY + "=" + JaxRsComponent.GLOBAL_COMPONENT_FACTORY + ")",
      cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, policy = ReferencePolicy.DYNAMIC)
  private ConcurrentMap<ServiceReference, ComponentInstance> globalJaxRsComponentInstances = new ConcurrentHashMap<>();
  private ConcurrentMap<ServiceReference, Object> globalJaxRsComponents = new ConcurrentHashMap<>();
  private Set<ServiceReference<ComponentFactory>> serviceReferencesDuringStartup = Sets.newConcurrentHashSet();

  private static final Logger log = LoggerFactory.getLogger(ServletContainerBridge.class);

  @Activate
  void activate(ComponentContext componentContext) {
    // bundle which contains the JAX-RS services
    bundle = (Bundle)componentContext.getProperties().get(PROPERTY_BUNDLE);
    bundleContext = bundle.getBundleContext();
    applicationPath = ApplicationPath.get(bundle);

    // delayed registering of references that where added before activation
    for (ServiceReference<ComponentFactory> serviceReference : serviceReferencesDuringStartup) {
      bindGlobalJaxRsComponentFactory(serviceReference);
    }

    // initialize component tracker to detect JAX-RS components in current bundle
    localComponents = Sets.newConcurrentHashSet();
    localComponentTracker = new JaxRsComponentTracker();
    localComponentTracker.open();

    // initialize JAX-RS application and Jersey Servlet container
    application = new JaxRsApplication(localComponents, globalJaxRsComponents.values());
    servletContainer = new ServletContainer(ResourceConfig.forApplication(application));
  }

  @Deactivate
  void deactivate(ComponentContext componentContext) {
    if (localComponentTracker != null) {
      localComponentTracker.close();
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

  void bindGlobalJaxRsComponentFactory(ServiceReference<ComponentFactory> serviceReference) {
    if (bundleContext == null) {
      serviceReferencesDuringStartup.add(serviceReference);
      return;
    }

    // create new JAX-RS component from OSGi factory service for each global component
    ComponentFactory componentFactory = bundleContext.getService(serviceReference);
    Dictionary<String, Object> props = new Hashtable<>();
    props.put(PROPERTY_GLOBAL_COMPONENT, true);
    props.put(ApplicationPath.PROPERTY_APPLICATON_PATH, applicationPath);
    ComponentInstance componentInstance = componentFactory.newInstance(props);
    Object component = componentInstance.getInstance();
    globalJaxRsComponentInstances.put(serviceReference, componentInstance);
    globalJaxRsComponents.put(serviceReference, componentInstance.getInstance());
    isDirty = true;
    log.debug("Registered global component: {} for {}", component.getClass().getName(), bundle.getSymbolicName());
  }

  void unbindGlobalJaxRsComponentFactory(ServiceReference<ComponentFactory> serviceReference) {
    Object component = globalJaxRsComponents.remove(serviceReference);
    ComponentInstance componentInstance = globalJaxRsComponentInstances.remove(serviceReference);
    if (componentInstance != null) {
      componentInstance.dispose();
    }
    isDirty = true;
    if (component != null) {
      log.debug("Unregistered global component: {} for {}", component.getClass().getName(), bundle.getSymbolicName());
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
      if (reference.getBundle() == bundle && !isGlobalComponentFactory(reference)) {
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
      if (reference.getBundle() == bundle && !isGlobalComponentFactory(reference)) {
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

    private boolean isJaxRsComponent(Object serviceInstance) {
      return (serviceInstance instanceof JaxRsComponent && hasAnyJaxRsAnnotation(serviceInstance.getClass()));
    }

    private boolean hasAnyJaxRsAnnotation(Class<?> clazz) {
      return clazz.isAnnotationPresent(Path.class)
          || clazz.isAnnotationPresent(Provider.class)
          || clazz.isAnnotationPresent(PreMatching.class);
    }

    private boolean isGlobalComponentFactory(ServiceReference serviceReference) {
      return PropertiesUtil.toBoolean(serviceReference.getProperty(PROPERTY_GLOBAL_COMPONENT), false);
    }

  }


}
