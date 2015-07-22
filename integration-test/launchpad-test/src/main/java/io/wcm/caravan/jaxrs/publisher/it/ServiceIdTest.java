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
import org.apache.sling.junit.annotations.SlingAnnotationsTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Calls REST interfaces of both sample services to get their service id.
 */
@RunWith(SlingAnnotationsTestRunner.class)
public class ServiceIdTest {

  private static final String SERVER_URL = "http://localhost:" + System.getProperty("org.osgi.service.http.port");

  /**
   * Sample Service 1
   */
  /*
  @Test
  public void testSampleService1() throws IOException {
    assertResponse("/caravan/jaxrs/test/sampleservice1/serviceId",
        "/caravan/jaxrs/test/sampleservice1", "text/plain");
  }
   */

  /**
   * Sample Service 1 - Not Found
   */
  @Test
  public void testSampleService1NotFound() throws IOException {
    assertNotFound("/caravan/jaxrs/test/sampleservice1/invalidPath");
  }

  /**
   * Sample Service 2
   */
  /*
  @Test
  public void testSampleService2() throws IOException {
    assertResponse("/caravan/jaxrs/test/sampleservice2/serviceId",
        "/caravan/jaxrs/test/sampleservice2", "text/plain");
  }
   */

  private void assertResponse(String url, String expectedResponse, String expectedContentType) throws IOException {
    String fullUrl = SERVER_URL + url;
    HttpGet get = new HttpGet(fullUrl);
    HttpResponse response = new DefaultHttpClient().execute(get);
    assertEquals("Response code for " + fullUrl, HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
    assertEquals("Content type for " + fullUrl, expectedContentType, response.getFirstHeader("Content-Type"));
    assertEquals("Response for " + fullUrl, expectedResponse, EntityUtils.toString(response.getEntity()));
  }

  private void assertNotFound(String url) throws IOException {
    String fullUrl = SERVER_URL + url;
    HttpGet get = new HttpGet(fullUrl);
    HttpResponse response = new DefaultHttpClient().execute(get);
    assertEquals("Response code for " + fullUrl, HttpServletResponse.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
  }

}
