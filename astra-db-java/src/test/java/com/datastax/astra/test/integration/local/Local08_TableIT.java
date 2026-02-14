package com.datastax.astra.test.integration.local;

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

import com.datastax.astra.test.integration.AbstractTableIT;
import com.datastax.astra.test.integration.utils.EnabledIfAstra;
import com.datastax.astra.test.integration.utils.EnabledIfLocalAvailable;
import org.junit.jupiter.api.DisplayName;

/**
 * Table integration tests for Astra DB environment.
 * <p>
 * These tests run against Astra DB when configured via:
 * - test.environment=astra_prod (or astra_dev, astra_test) in test-config.properties
 * - Or environment variable ASTRA_DB_JAVA_TEST_ENV=astra_prod
 * <p>
 * Requires ASTRA_DB_APPLICATION_TOKEN to be set.
 */
@EnabledIfLocalAvailable
@DisplayName("08. HCD Table")
public class Local08_TableIT extends AbstractTableIT {
    // All tests inherited from AbstractTableIT
}
