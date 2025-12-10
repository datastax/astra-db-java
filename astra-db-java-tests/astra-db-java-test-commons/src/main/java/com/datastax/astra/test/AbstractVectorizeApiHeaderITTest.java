package com.datastax.astra.test;

import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.commands.options.CollectionFindOneOptions;
import com.datastax.astra.client.collections.commands.options.CollectionInsertManyOptions;
import com.datastax.astra.client.collections.commands.results.CollectionInsertManyResult;
import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.DataAPIKeywords;
import com.datastax.astra.client.core.headers.EmbeddingAPIKeyHeaderProvider;
import com.datastax.astra.client.core.query.Projection;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.datastax.astra.client.core.vectorize.EmbeddingProvider;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public abstract class AbstractVectorizeApiHeaderITTest extends AbstractVectorizeITTest {

    /**
     * Find the Embedding Provider if it exists.
     */
    @Test
    public void should_test_vectorize_with_api_header() {
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
     * Coming from findEmbeddingProviders, test the provider.
     * @param provider
     *      provider information
     */
    public void testVectorizeWithApiKeyHeader(EmbeddingProvider provider) {
        log.info("Testing embedding provider {}", getEmbeddingProviderId());

        // Header authentication
        final EmbeddingAPIKeyHeaderProvider authProvider = (getApiKey() != null) ?
                new EmbeddingAPIKeyHeaderProvider(getApiKey()) : null;

        // Test for each model
        provider.getModels().forEach(model -> {
            try {
                log.info("Testing model {}", model);

                // (1) Create a collection with a name matching the model
                CollectionDefinition definition = new CollectionDefinition()
                        .vectorSimilarity(SimilarityMetric.COSINE)
                        .vectorize(getEmbeddingProviderId(), model.getName(), null, getAuthenticationParameters());
                if (model.getVectorDimension() != null) {
                    definition.vectorDimension(model.getVectorDimension());
                }
                String collectionName = getCollectionNameFromModel(model.getName());
                Collection<Document> collection =  getDatabase().createCollection(collectionName, definition);
                log.info("Collection created {}", collection.getCollectionName());

                // (2) Ingestion
                CollectionInsertManyOptions options = new CollectionInsertManyOptions();
                if (authProvider != null) {
                    options.embeddingAuthProvider(authProvider);
                }
                CollectionInsertManyResult res = collection.insertMany(TestDataset.DOCS_SONG_DIRE_STRAITS, options);
                assertThat(res.getInsertedIds()).hasSize(8);
                log.info("{} Documents inserted", res.getInsertedIds().size());

                // (3) Find with Vectorize
                Optional<Document> doc = collection.findOne(null,
                        new CollectionFindOneOptions()
                                .sort(Sort.vectorize("You shouldn't come around here singing up at people like tha"))
                                .projection(Projection.exclude(DataAPIKeywords.VECTOR.getKeyword()))
                                .embeddingAuthProvider(authProvider)
                                .includeSimilarity(true));
                log.info("Document found {}", doc);
                assertThat(doc).isPresent();
                assertThat(doc.get().getId(Integer.class)).isEqualTo(7);
                assertThat(doc.get().getDouble(DataAPIKeywords.SIMILARITY.getKeyword())).isGreaterThan(.8);

                collection.drop();
            } catch(Exception e) {
                log.error("Error while testing model {}", model, e);
            }
        });
    }


}
