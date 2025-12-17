/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2016 wcm.io
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
package org.apache.sling.junit.teleporter.customizers;

import org.apache.sling.commons.testing.integration.HttpTest;
import org.apache.sling.junit.rules.TeleporterRule;
import org.apache.sling.testing.teleporter.client.ClientSideTeleporter;

import static org.junit.Assert.fail;

/** TeleporterRule Customizer used for Sling launchpad integration tests.
 *  Waits for Sling to be ready and sets the appropriate parameters
 *  on the ClientSideTeleporter.
 */
public class ITCustomizer implements TeleporterRule.Customizer {

    private static final HttpTest H = new HttpTest();
    private static final int TEST_READY_TIMEOUT_SECONDS = Integer.getInteger("ClientSideTeleporter.testReadyTimeoutSeconds", 12);

    @Override
    /** Customize the client-side TeleporterRule by first waiting
     *  for Sling to be ready and then setting it up with the test server
     *  URL, timeout etc.
     */
    public void customize(TeleporterRule t, String options) {
        // Setup Sling and the ClientSideTeleporter
        try {
            H.setUp();
        } catch (Exception e) {
            fail("HttpTest setup failed: " + e);
        }
        final ClientSideTeleporter cst = (ClientSideTeleporter)t;
        cst.setBaseUrl(HttpTest.HTTP_BASE_URL);
        cst.setTestReadyTimeoutSeconds(TEST_READY_TIMEOUT_SECONDS);

        cst.setServerCredentials("admin", "admin");
    }
}
