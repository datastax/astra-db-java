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

import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.commands.results.CollectionInsertOneResult;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.test.integration.AbstractCollectionIT;
import com.datastax.astra.test.integration.utils.EnabledIfAstra;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.datastax.astra.test.integration.utils.TestDataset.*;

/**
 * Collection integration tests against Astra DB.
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
@DisplayName("Astra / Collections / CRUD")
public class Astra_Collections_01_CrudIT extends AbstractCollectionIT {

    /** Tested collection for typed beans with vector. */
    protected Collection<Document> collectionVectorize;

    @BeforeAll
    void setupCollections_vectorize() {
        collectionVectorize = getDatabase().createCollection(
                COLLECTION_VECTORIZE, COLLECTION_VECTORIZE_DEF);
    }

    @Test
    void should_sort_vector_on_replaceOne() {
        collectionVectorize.deleteAll();
        Document document = new Document()
                .append("name", "Jane Doe")
                .append("$vectorize", "Text to vectorize");
        CollectionInsertOneResult result = collectionVectorize.insertOne(document);
        System.out.println(result.getInsertedId());
        // Introduce Sorting on replaceOne to ensure it doesn't interfere with vector search
    }




}
