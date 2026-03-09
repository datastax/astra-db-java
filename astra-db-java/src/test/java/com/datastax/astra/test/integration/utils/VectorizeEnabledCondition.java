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

/**
 * JUnit 5 condition that enables tests only when vectorize feature is enabled.
 * The vectorize feature is controlled by the {@code test.vectorize} property in test configuration.
 */
public class VectorizeEnabledCondition implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        TestConfig config = TestConfig.getInstance();
        if (config.isVectorizeEnabled()) {
            return ConditionEvaluationResult.enabled("Vectorize feature is enabled (test.vectorize=true)");
        }
        return ConditionEvaluationResult.disabled("Skipping: vectorize feature is not enabled (test.vectorize=false or not set)");
    }
}
