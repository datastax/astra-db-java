package com.datastax.astra.test.integration.astra;

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

import com.datastax.astra.test.integration.AbstractDatabaseAdminIT;
import com.datastax.astra.test.integration.utils.EnabledIfAstra;
import org.junit.jupiter.api.DisplayName;

/**
 * Database Admin integration tests against Astra DB.
 * <p>
 * Tests require:
 * <ul>
 *   <li>test.environment set to astra_dev, astra_prod, or astra_test</li>
 *   <li>Valid Astra token in test-config-astra.properties or ASTRA_DB_APPLICATION_TOKEN env var</li>
 * </ul>
 * <p>
 * Tests are skipped automatically if not configured for Astra or token is missing.
 */
@EnabledIfAstra
@DisplayName("Astra / DatabaseAdmin")
public class Astra_02_DatabaseAdminIT extends AbstractDatabaseAdminIT {
}
