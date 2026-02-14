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

import com.datastax.astra.test.integration.AbstractCollectionDDLIT;
import com.datastax.astra.test.integration.utils.EnabledIfLocalAvailable;
import org.junit.jupiter.api.DisplayName;

/**
 * Database integration tests against local HCD/DSE.
 * <p>
 * Tests require a local HCD or DSE instance running at http://localhost:8181.
 * Start with: docker-compose up -d
 * <p>
 * Tests are skipped automatically if local instance is not available.
 */
@EnabledIfLocalAvailable
@DisplayName("03. HCD Database Integration Tests")
public class HCD_Collections_00_SchemaIT extends AbstractCollectionDDLIT {
}
