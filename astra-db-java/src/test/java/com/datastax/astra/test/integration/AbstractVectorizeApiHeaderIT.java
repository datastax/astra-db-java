package com.datastax.astra.test.integration;

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
import com.datastax.astra.client.collections.commands.options.CollectionFindOneOptions;
import com.datastax.astra.client.collections.commands.options.CollectionInsertManyOptions;
import com.datastax.astra.client.collections.commands.results.CollectionInsertManyResult;
import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.DataAPIKeywords;
import com.datastax.astra.client.core.headers.EmbeddingAPIKeyHeaderProvider;
import com.datastax.astra.client.core.headers.EmbeddingHeadersProvider;
import com.datastax.astra.client.core.query.Projection;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.datastax.astra.client.core.vectorize.EmbeddingProvider;
import com.datastax.astra.test.integration.utils.TestDataset;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Abstract base class for vectorize integration tests using API key header authentication.
 * <p>
 * This class extends {@link AbstractVectorizeIT} and provides test methods that pass
 * the embedding API key as a header with each request, rather than using a shared key
 * stored in Astra.
 * <p>
 * Subclasses should implement the abstract methods to provide the specific API key
 * and provider configuration for testing.
 */
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractVectorizeApiHeaderIT extends AbstractVectorizeIT {

    /**
     * Test vectorize functionality with API key passed as a header.
     * <p>
     * This test:
     * <ol>
     *   <li>Finds the embedding provider configuration</li>
     *   <li>For each model, creates a collection with vectorize enabled</li>
     *   <li>Inserts documents with embeddings generated via the API</li>
     *   <li>Performs a vector search to validate the embeddings</li>
     * </ol>
     */
    protected void should_test_vectorize_with_api_header() {
        getDatabase()
                .getDatabaseAdmin()
                .findEmbeddingProviders()
                .getEmbeddingProviders()
                .computeIfPresent(getEmbeddingProviderId(), (key, value) -> {
                    this.testVectorizeWithApiKeyHeader(value);
                    return value;
                });
    }

    /**
     * Returns the embedding authentication provider for this test.
     * <p>
     * By default, creates an {@link EmbeddingAPIKeyHeaderProvider} from {@link #getApiKey()}.
     * Subclasses can override this to provide a different authentication mechanism
     * (e.g., {@link com.datastax.astra.client.core.headers.AWSEmbeddingHeadersProvider}).
     *
     * @return the embedding headers provider, or {@code null} if no API key is configured
     */
    protected EmbeddingHeadersProvider getEmbeddingAuthProvider() {
        return (getApiKey() != null) ? new EmbeddingAPIKeyHeaderProvider(getApiKey()) : null;
    }

    /**
     * Internal method to test vectorize with API key header authentication.
     *
     * @param provider the embedding provider configuration
     */
    protected void testVectorizeWithApiKeyHeader(EmbeddingProvider provider) {
        log.info("Testing embedding provider: {}", getEmbeddingProviderId());

        // Header authentication provider
        final EmbeddingHeadersProvider authProvider = getEmbeddingAuthProvider();

        // Test for each model
        provider.getModels().forEach(model -> {
            try {
                log.info("Testing model: {}", model.getName());

                // (1) Create a collection with vectorize enabled
                CollectionDefinition definition = new CollectionDefinition()
                        .vectorSimilarity(SimilarityMetric.COSINE)
                        .vectorize(getEmbeddingProviderId(), model.getName(), null, getAuthenticationParameters());
                if (model.getVectorDimension() != null) {
                    definition.vectorDimension(model.getVectorDimension());
                }
                String collectionName = getCollectionNameFromModel(model.getName());
                Collection<Document> collection = getDatabase().createCollection(collectionName, definition);
                log.info("Collection created: {}", collection.getCollectionName());

                // (2) Ingestion with API key header
                CollectionInsertManyOptions insertOptions = new CollectionInsertManyOptions();
                if (authProvider != null) {
                    insertOptions.embeddingAuthProvider(authProvider);
                }
                CollectionInsertManyResult res = collection.insertMany(TestDataset.DOCS_SONG_DIRE_STRAITS, insertOptions);
                assertThat(res.getInsertedIds()).hasSize(8);
                log.info("{} Documents inserted", res.getInsertedIds().size());

                // (3) Find with Vectorize and API key header
                Optional<Document> doc = collection.findOne(null,
                        new CollectionFindOneOptions()
                                .sort(Sort.vectorize("You shouldn't come around here singing up at people like that"))
                                .projection(Projection.exclude(DataAPIKeywords.VECTOR.getKeyword()))
                                .embeddingAuthProvider(authProvider)
                                .includeSimilarity(true));

                log.info("Document found: {}", doc);
                assertThat(doc).isPresent();
                assertThat(doc.get().getId(Integer.class)).isEqualTo(7);
                assertThat(doc.get().getDouble(DataAPIKeywords.SIMILARITY.getKeyword())).isGreaterThan(.8);

                collection.drop();
            } catch (Exception e) {
                log.error("Error while testing model {}", model.getName(), e);
            }
        });
    }
}
