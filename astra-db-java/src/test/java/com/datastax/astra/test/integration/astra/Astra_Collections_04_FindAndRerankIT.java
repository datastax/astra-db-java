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

import com.datastax.astra.test.integration.AbstractCollectionFindAndRerankIT;
import com.datastax.astra.test.integration.utils.EnabledIfAstra;
import org.junit.jupiter.api.DisplayName;

/**
 * FindAndRerank integration tests against Astra DB.
 * <p>
 * Uses Nvidia NV-Embed-QA (built-in, no external API key) for embedding
 * and Nvidia reranking (uses the Astra token).
 */
@EnabledIfAstra
@DisplayName("Astra / Collections / Hybrid Search")
public class Astra_Collections_04_FindAndRerankIT extends AbstractCollectionFindAndRerankIT {

    @Override
    protected String getRerankingApiKey() {
        // Nvidia reranking uses the Astra token
        return getConfig().getAstraToken();
    }
}
