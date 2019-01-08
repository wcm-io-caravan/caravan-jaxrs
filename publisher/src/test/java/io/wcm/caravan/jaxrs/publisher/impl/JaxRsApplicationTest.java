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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.common.collect.ImmutableSet;

import io.wcm.caravan.jaxrs.publisher.JaxRsComponent;


@RunWith(MockitoJUnitRunner.class)
public class JaxRsApplicationTest {

  @Mock
  private JaxRsComponent localComponent;
  @Mock
  private JaxRsComponent globalComponent;

  @Test
  public void test_getSingletons() throws Exception {

    JaxRsApplication app = new JaxRsApplication(ImmutableSet.of(localComponent), ImmutableSet.of(globalComponent));
    Set<Object> singletons = app.getSingletons();

    assertEquals(2, singletons.size());
    assertTrue(singletons.contains(localComponent));
    assertTrue(singletons.contains(globalComponent));
  }

  @Test
  public void test_getClasses_from_local_components() throws Exception {

    when(localComponent.getAdditionalJaxRsClassesToRegister())
        .thenReturn(ImmutableSet.of(AdditionalResource.class));

    JaxRsApplication app = new JaxRsApplication(ImmutableSet.of(localComponent), ImmutableSet.of(globalComponent));
    Set<Class<?>> additionalClasses = app.getClasses();

    assertEquals(1, additionalClasses.size());
    assertTrue(additionalClasses.contains(AdditionalResource.class));
  }

  @Test
  public void test_getClasses_from_global_components() throws Exception {

    when(globalComponent.getAdditionalJaxRsClassesToRegister())
        .thenReturn(ImmutableSet.of(AdditionalResource.class));

    JaxRsApplication app = new JaxRsApplication(ImmutableSet.of(localComponent), ImmutableSet.of(globalComponent));
    Set<Class<?>> additionalClasses = app.getClasses();

    assertEquals(1, additionalClasses.size());
    assertTrue(additionalClasses.contains(AdditionalResource.class));
  }

  @Path("/foo")
  static class AdditionalResource {

    @GET
    public String get() {
      return "bar";
    }
  }
}
