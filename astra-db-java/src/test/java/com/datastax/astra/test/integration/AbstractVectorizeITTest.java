package com.datastax.astra.test.integration;

import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.collections.commands.options.CollectionFindOneOptions;
import com.datastax.astra.client.collections.commands.options.CollectionInsertManyOptions;
import com.datastax.astra.client.collections.commands.results.CollectionInsertManyResult;
import com.datastax.astra.client.core.headers.EmbeddingAPIKeyHeaderProvider;
import com.datastax.astra.client.core.query.Projection;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.DataAPIKeywords;
import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.datastax.astra.client.core.vectorize.EmbeddingProvider;
import com.datastax.astra.test.model.TestDataset;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public abstract class AbstractVectorizeITTest extends AbstractDataAPITest {

    protected abstract String getApiKey();

    protected abstract String getEmbeddingProviderId();

    protected abstract Map<String, Object> getAuthenticationParameters();

    /**
     * Pick the Proper API KEY for testing a MODEL.
     *
     * @param provider
     *      provider name
     * @return
     *      value
     */
    private static String getApiKey(String provider) {
        return switch (provider) {
            case "azureOpenAI"  -> System.getenv("AZURE_OPENAI_API_KEY");
            default -> null;
        };
    }

    /**
     * Get the parameters for the provider.
     *
     * @param provider
     *      current provider
     * @return
     *      parameters
     */
    private Map<String, Object> getAuthenticationParameters(String provider) {
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
    // Shared KEY
    // ===================================================================================

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

    private void testEmbeddingProviderSharedKey(String providerName, EmbeddingProvider provider, String keyName) {
        log.info("Testing embedding provider with a Share providerName in ASTRA {}", providerName);
        dropAllCollections();
        Map<String, Object> params = getAuthenticationParameters(providerName);

        // Loop on each model
        provider.getModels().forEach(model -> {
           log.info("[" + providerName +"/"+ model + "] Start Test");

                // (1) Create a collection with a name matching the model
                CollectionDefinition cd = new CollectionDefinition()
                        .vectorSimilarity(SimilarityMetric.COSINE)
                        .vectorDimension(model.getVectorDimension() != null ? model.getVectorDimension() : 0)
                        .vectorize(providerName, model.getName(), keyName, params);
                Collection<Document> collection = getDatabase()
                        .createCollection(getCollectionNameFromModel(model.getName()), cd, Document.class);
                log.info("Collection created {}", collection.getCollectionName());

                // (2) Ingestion with the Shared KEY ( no need to add an header)
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

    protected String getCollectionNameFromModel(String modelName) {
        String name=  modelName.toLowerCase()
                .replaceAll("-", "_")
                .replaceAll("/", "_")
                .replaceAll("\\.", "");
        // Truncate
        name = name.substring(0,Math.min(name.length(), 25));
        return name;
    }

}
