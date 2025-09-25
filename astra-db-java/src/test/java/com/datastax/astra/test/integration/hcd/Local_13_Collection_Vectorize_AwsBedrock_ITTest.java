package com.datastax.astra.test.integration.hcd;

import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.commands.options.CollectionFindOneOptions;
import com.datastax.astra.client.collections.commands.options.CollectionInsertManyOptions;
import com.datastax.astra.client.collections.commands.results.CollectionInsertManyResult;
import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.DataAPIKeywords;
import com.datastax.astra.client.core.headers.AWSEmbeddingHeadersProvider;
import com.datastax.astra.client.core.headers.EmbeddingHeadersProvider;
import com.datastax.astra.client.core.query.Projection;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.datastax.astra.test.integration.AbstractVectorizeITTest;
import com.datastax.astra.test.model.TestDataset;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@EnabledIfSystemProperty(named = "ASTRA_DB_JAVA_TEST_ENV", matches = "local")
@EnabledIfSystemProperty(named = "BEDROCK_HEADER_AWS_ACCESS_ID", matches = ".*")
@EnabledIfSystemProperty(named = "BEDROCK_HEADER_AWS_SECRET_ID", matches = ".*")
@EnabledIfSystemProperty(named = "BEDROCK_HEADER_AWS_REGION", matches = ".*")
public class Local_13_Collection_Vectorize_AwsBedrock_ITTest extends AbstractVectorizeITTest {

    public static final String MODEL_NAME = "amazon.titan-embed-text-v1";

    @Override
    protected String getEmbeddingProviderId() {
        return "bedrock";
    }

    @Override
    protected String getApiKey() {
        return null;
    }

    @Override
    protected Map<String, Object> getAuthenticationParameters() {
        return  Map.of("region", System.getenv("BEDROCK_HEADER_AWS_REGION"));
    }

    /**
     * Find the Embedding Provider if it exists.
     */
    @Test
    public void should_test_vectorize_with_api_header() {
        dropAllCollections();
        // Header authentication
        EmbeddingHeadersProvider authProvider = new AWSEmbeddingHeadersProvider(
                readEnvVariable("BEDROCK_HEADER_AWS_ACCESS_ID"),
                readEnvVariable("BEDROCK_HEADER_AWS_SECRET_ID")
        );

        // (1) Create a collection with a name matching the model
        CollectionDefinition definition = new CollectionDefinition()
                .vectorSimilarity(SimilarityMetric.COSINE)
                .vectorize(getEmbeddingProviderId(), MODEL_NAME, null, getAuthenticationParameters());

        Collection<Document> collection =  getDatabase().createCollection("aws_bedrock", definition);
        log.info("Collection created {}", collection.getCollectionName());

        // (2) Ingestion
        CollectionInsertManyOptions options = new CollectionInsertManyOptions().embeddingAuthProvider(authProvider);
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

    }

}
