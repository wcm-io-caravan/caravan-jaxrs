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

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;

/**
 * Gets JAX-RS application path (URI prefix) from OSGi header of the current bundle.
 */
public final class ApplicationPath {

  /**
   * OSGi bundle header name that defines the "application path" (URI prefix) for all JAX-RS services in the bundle.
   */
  public static final String HEADER_APPLICATON_PATH = "Caravan-JaxRs-ApplicationPath";

  /**
   * OSGi service property name that defines the "application path" (URI prefix).
   * This property set is automatically set for "global" JAX-RS components.
   */
  public static final String PROPERTY_APPLICATON_PATH = "caravan.jaxrs.applicationPath";

  private ApplicationPath() {
    // static methods only
  }

  /**
   * Get JAX-RS application path for bundle.
   * @param bundle Bundle
   * @return Application path or null if not defined
   */
  public static String get(Bundle bundle) {
    return bundle.getHeaders().get(HEADER_APPLICATON_PATH);
  }

  /**
   * Get JAX-RS application path for bundle or bundle context.
   * @param bundleContext Bundle context
   * @return Application path or null if not defined
   */
  public static String get(BundleContext bundleContext) {
    return get(bundleContext.getBundle());
  }

  /**
   * Get JAX-RS application path for bundle of component context.
   * If the component context is from an "global" JAx-RS Component the application path for which the
   * service instance was created is returned.
   * @param componentContext Component Context
   * @return Application path or null if not defined
   */
  public static String get(ComponentContext componentContext) {
    Bundle usingBundle = componentContext.getUsingBundle();
    if (usingBundle != null) {
      return get(usingBundle);
    }
    return get(componentContext.getBundleContext().getBundle());
  }

}
