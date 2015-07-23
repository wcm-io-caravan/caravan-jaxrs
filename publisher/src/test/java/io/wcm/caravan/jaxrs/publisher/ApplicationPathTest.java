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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Dictionary;
import java.util.Hashtable;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationPathTest {

  private static final String TEST_PATH = "/test/path";
  private static final String TEST_PATH_2 = "/test/path2";

  @Mock
  private Bundle bundle;
  @Mock
  private BundleContext bundleContext;
  @Mock
  private ComponentContext componentContext;

  @Before
  public void setUp() {
    Dictionary<String, String> headers = new Hashtable<>();
    headers.put(ApplicationPath.HEADER_APPLICATON_PATH, TEST_PATH);
    when(bundle.getHeaders()).thenReturn(headers);

    when(bundleContext.getBundle()).thenReturn(bundle);
    when(componentContext.getBundleContext()).thenReturn(bundleContext);
    when(componentContext.getProperties()).thenReturn(new Hashtable<>());
  }

  @Test
  public void testBundle() {
    assertEquals(TEST_PATH, ApplicationPath.get(bundle));
  }

  @Test
  public void testBundle_HeaderNotSet() {
    when(bundle.getHeaders()).thenReturn(new Hashtable<String, String>());
    assertNull(ApplicationPath.get(bundle));
  }

  @Test
  public void testBundleContext() {
    assertEquals(TEST_PATH, ApplicationPath.get(bundleContext));
  }

  @Test
  public void testComponentContext() {
    assertEquals(TEST_PATH, ApplicationPath.get(componentContext));
  }

  @Test
  public void testComponentContext_UsingBundle() {
    Bundle usingBundle = mock(Bundle.class);
    Dictionary<String, String> headers = new Hashtable<>();
    headers.put(ApplicationPath.HEADER_APPLICATON_PATH, TEST_PATH_2);
    when(usingBundle.getHeaders()).thenReturn(headers);

    when(componentContext.getUsingBundle()).thenReturn(usingBundle);
    assertEquals(TEST_PATH_2, ApplicationPath.get(componentContext));
  }

}
