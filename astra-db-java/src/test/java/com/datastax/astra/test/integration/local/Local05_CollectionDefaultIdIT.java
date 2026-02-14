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

import com.datastax.astra.test.integration.AbstractCollectionDefaultIdIT;
import com.datastax.astra.test.integration.utils.EnabledIfLocalAvailable;
import org.junit.jupiter.api.DisplayName;

/**
 * Collection default ID type integration tests against Astra DB.
 * <p>
 * Tests UUID, ObjectId, UUIDv6, and UUIDv7 as default ID types.
 */
@EnabledIfLocalAvailable
@DisplayName("05. HCD Collection Default IDs")
public class Local05_CollectionDefaultIdIT extends AbstractCollectionDefaultIdIT {
}
