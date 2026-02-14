package com.datastax.astra.test.integration.utils;

/*-
 * #%L
 * Data API Java Client
 * --
 * Copyright (C) 2024 DataStax
 * --
 * Licensed under the Apache License, Version 2.0
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * JUnit 5 condition that enables tests only when local HCD/DSE is actually available.
 * This checks if the local endpoint is reachable before running tests.
 */
public class LocalAvailableCondition implements ExecutionCondition {

    private static Boolean isAvailable = null;

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        TestConfig config = TestConfig.getInstance();

        // Only check local availability if we're in local mode
        if (!config.isLocal()) {
            return ConditionEvaluationResult.disabled("Not in local mode (current: " + config.getEnvironment() + ")");
        }

        // Cache the availability check
        if (isAvailable == null) {
            isAvailable = checkLocalAvailability(config.getLocalEndpoint());
        }

        if (isAvailable) {
            return ConditionEvaluationResult.enabled("Local HCD/DSE is available at " + config.getLocalEndpoint());
        }
        return ConditionEvaluationResult.disabled("Local HCD/DSE is not available at " + config.getLocalEndpoint());
    }

    private boolean checkLocalAvailability(String endpoint) {
        try {
            URL url = new URL(endpoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);
            int responseCode = connection.getResponseCode();
            return responseCode >= 200 && responseCode < 500;
        } catch (Exception e) {
            return false;
        }
    }
}
