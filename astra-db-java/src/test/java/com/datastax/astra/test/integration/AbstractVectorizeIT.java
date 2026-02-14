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
import com.datastax.astra.client.collections.commands.results.CollectionInsertManyResult;
import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.DataAPIKeywords;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.datastax.astra.client.core.vectorize.EmbeddingProvider;
import com.datastax.astra.test.integration.utils.TestDataset;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Abstract base class for vectorize integration tests.
 * <p>
 * Subclasses must implement methods to provide API key, embedding provider ID,
 * and authentication parameters for the specific provider being tested.
 * <p>
 * This class provides test methods for validating vectorize functionality
 * with both shared key authentication and API header authentication.
 */
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractVectorizeIT extends AbstractDataAPITest {

    @BeforeAll
    void setupCleanKeyspace() {
        dropAllCollections();
        dropAllTables();
    }

    /**
     * Get the API key for the embedding provider.
     *
     * @return the API key, or null if using shared key authentication
     */
    protected abstract String getApiKey();

    /**
     * Get the embedding provider ID (e.g., "openai", "nvidia", "huggingface").
     *
     * @return the provider ID
     */
    protected abstract String getEmbeddingProviderId();

    /**
     * Get additional authentication parameters for the provider.
     * <p>
     * Some providers like Azure OpenAI require additional parameters
     * such as deploymentId and resourceName.
     *
     * @return authentication parameters map, or null if not required
     */
    protected abstract Map<String, Object> getAuthenticationParameters();

    /**
     * Get the API key for a specific provider.
     * Override this method to provide custom key lookup logic.
     *
     * @param provider the provider name
     * @return the API key, or null if not found
     */
    protected String getApiKey(String provider) {
        return switch (provider) {
            case "azureOpenAI" -> System.getenv("AZURE_OPENAI_API_KEY");
            default -> null;
        };
    }

    /**
     * Get authentication parameters for a specific provider.
     *
     * @param provider the provider name
     * @return the authentication parameters, or null if not required
     */
    protected Map<String, Object> getAuthenticationParameters(String provider) {
        switch (provider) {
            case "azureOpenAI":
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("deploymentId", "text-embedding-3-small-steo");
                parameters.put("resourceName", "steo-azure-openai");
                return parameters;
            default:
                return null;
        }
    }

    // ===================================================================================
    // Shared Key Tests
    // ===================================================================================

    /**
     * Test vectorize functionality using a shared key stored in Astra.
     *
     * @param provider the provider ID
     * @param keyName  the name of the shared key
     */
    public void testVectorizeWithSharedKey(String provider, String keyName) {
        getDatabase()
                .getDatabaseAdmin()
                .findEmbeddingProviders()
                .getEmbeddingProviders()
                .computeIfPresent(provider, (key, value) -> {
                    this.testEmbeddingProviderSharedKey(key, value, keyName);
                    return value;
                });
    }

    /**
     * Internal method to test an embedding provider with a shared key.
     *
     * @param providerName the provider name
     * @param provider     the embedding provider configuration
     * @param keyName      the shared key name
     */
    private void testEmbeddingProviderSharedKey(String providerName, EmbeddingProvider provider, String keyName) {
        log.info("Testing embedding provider with shared key in ASTRA: {}", providerName);
        dropAllCollections();
        Map<String, Object> params = getAuthenticationParameters(providerName);

        // Loop on each model
        provider.getModels().forEach(model -> {
            log.info("[{}/{}] Start Test", providerName, model.getName());

            // (1) Create a collection with a name matching the model
            CollectionDefinition cd = new CollectionDefinition()
                    .vectorSimilarity(SimilarityMetric.COSINE)
                    .vectorDimension(model.getVectorDimension() != null ? model.getVectorDimension() : 0)
                    .vectorize(providerName, model.getName(), keyName, params);
            Collection<Document> collection = getDatabase()
                    .createCollection(getCollectionNameFromModel(model.getName()), cd, Document.class);
            log.info("Collection created: {}", collection.getCollectionName());

            // (2) Ingestion with the Shared KEY (no need to add an header)
            CollectionInsertManyResult res = collection
                    .insertMany(TestDataset.DOCS_SONG_DIRE_STRAITS);

            assertThat(res.getInsertedIds()).hasSize(8);
            log.info("{} Documents inserted", res.getInsertedIds().size());

            // (3) Find with Vectorize
            Optional<Document> doc = collection.findOne(null,
                    new CollectionFindOneOptions()
                            .sort(Sort.vectorize("You shouldn't come around here singing up at people like that"))
                            .includeSortVector(true)
                            .includeSimilarity(true));

            assertThat(doc).isPresent();
            assertThat(doc.get().getId(Integer.class)).isEqualTo(7);
            assertThat(doc.get().getDouble(DataAPIKeywords.SIMILARITY.getKeyword())).isGreaterThan(.8);

            collection.drop();
        });
    }

    /**
     * Generate a valid collection name from a model name.
     * Converts to lowercase, replaces special characters, and truncates to 25 chars.
     *
     * @param modelName the model name
     * @return a valid collection name
     */
    protected String getCollectionNameFromModel(String modelName) {
        String name = modelName.toLowerCase()
                .replaceAll("-", "_")
                .replaceAll("/", "_")
                .replaceAll("\\.", "");
        // Truncate to max 25 characters
        return name.substring(0, Math.min(name.length(), 25));
    }
}
