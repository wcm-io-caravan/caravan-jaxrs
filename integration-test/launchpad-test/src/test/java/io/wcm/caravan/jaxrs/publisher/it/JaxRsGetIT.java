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
package io.wcm.caravan.jaxrs.publisher.it;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

public class JaxRsGetIT {

  private static final String SERVER_URL = System.getProperty("launchpad.http.server.url");

  @Test
  public void testSampleService1NotFound() throws IOException {
    assertNotFound("/caravan/jaxrs/test/sampleservice1/invalidPath");
  }

  @Test
  public void testSampleService1() throws IOException {
    assertResponse("/caravan/jaxrs/test/sampleservice1/serviceId",
        "/caravan/jaxrs/test/sampleservice1", "text/plain");
  }

  @Test
  public void testSampleService2() throws IOException {
    assertResponse("/caravan/jaxrs/test/sampleservice2/serviceId",
        "/caravan/jaxrs/test/sampleservice2", "text/plain");
  }

  @Test
  public void testSampleService1Global() throws IOException {
    assertResponse("/caravan/jaxrs/test/sampleservice1/globalServiceId",
        "/caravan/jaxrs/test/sampleservice1", "text/plain");
  }

  @Test
  public void testSampleService2Global() throws IOException {
    assertResponse("/caravan/jaxrs/test/sampleservice2/globalServiceId",
        "/caravan/jaxrs/test/sampleservice2", "text/plain");
  }

  private void assertResponse(String url, String expectedResponse, String expectedContentType) throws IOException {
    String fullUrl = SERVER_URL + url;
    HttpGet get = new HttpGet(fullUrl);
    HttpResponse response = new DefaultHttpClient().execute(get);
    assertEquals("Response code for " + fullUrl, HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
    assertEquals("Content type for " + fullUrl, expectedContentType, response.getFirstHeader("Content-Type").getValue());
    assertEquals("Response for " + fullUrl, expectedResponse, EntityUtils.toString(response.getEntity()));
  }

  private void assertNotFound(String url) throws IOException {
    String fullUrl = SERVER_URL + url;
    HttpGet get = new HttpGet(fullUrl);
    HttpResponse response = new DefaultHttpClient().execute(get);
    assertEquals("Response code for " + fullUrl, HttpServletResponse.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
  }

}
