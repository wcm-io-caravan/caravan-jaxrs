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

import org.apache.sling.junit.remote.testrunner.SlingRemoteTestParameters;
import org.apache.sling.junit.remote.testrunner.SlingRemoteTestRunner;
import org.apache.sling.testing.tools.sling.SlingTestBase;
import org.junit.runner.RunWith;

@RunWith(SlingRemoteTestRunner.class)
public class RemoteIT extends SlingTestBase implements SlingRemoteTestParameters {

  @Override
  public String getJunitServletUrl() {
    return getServerBaseUrl() + "/system/sling/junit";
  }

  @Override
  public String getTestClassesSelector() {
    return "io.wcm.caravan.jaxrs.publisher";
  }

  @Override
  public String getTestMethodSelector() {
    return null;
  }

}
