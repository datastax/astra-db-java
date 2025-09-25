package com.datastax.astra.test.integration.astra.collections;

import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.databases.commands.results.FindEmbeddingProvidersResult;
import com.datastax.astra.test.integration.AbstractVectorizeITTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@EnabledIfEnvironmentVariable(named = "ASTRA_DB_APPLICATION_TOKEN_DEV", matches = "Astra.*")
@EnabledIfEnvironmentVariable(named = "ASTRA_CLOUD_PROVIDER_DEV", matches = ".*")
@EnabledIfEnvironmentVariable(named = "ASTRA_CLOUD_REGION_DEV", matches = ".*")
@EnabledIfEnvironmentVariable(named = "EMBEDDING_API_KEY", matches = ".*")
@EnabledIfEnvironmentVariable(named = "EMBEDDING_PROVIDER", matches = ".*")
@EnabledIfSystemProperty(named = "ASTRA_DB_JAVA_TEST_ENV", matches = "astra_dev")
@DisabledIfSystemProperty(named = "ASTRA_DB_JAVA_TEST_ENV", matches = "(?!astra_dev)")
public class Astra_09_VectorizeHuggingFaceDedicatedITTest extends AbstractVectorizeITTest {

    @Test
    public void testHuggingFaceDedicated() {
        String providerName   = System.getenv("EMBEDDING_PROVIDER");
        String collectionName = "collection_hf_dedicated";
        FindEmbeddingProvidersResult result = getDatabaseAdmin().findEmbeddingProviders();
        assertThat(result.getEmbeddingProviders().get(providerName)).isNotNull();

        // Create Collection
        CollectionDefinition cd = new CollectionDefinition()
                .vectorDimension(Integer.valueOf(System.getenv("HUGGINGFACEDED_DIMENSION")));
        Map<String, Object > params = new HashMap<>();
        params.put("endpointName",  System.getenv("HUGGINGFACEDED_ENDPOINTNAME"));
        params.put("regionName", System.getenv("HUGGINGFACEDED_REGIONNAME"));
        params.put("cloudName",  System.getenv("HUGGINGFACEDED_CLOUDNAME"));
        cd.vectorize(providerName, null, null, params);
        Collection<Document> collection = getDatabase().createCollection(collectionName, cd);
        assertThat(getDatabase().collectionExists(collectionName)).isTrue();

        // Test Collection
        //ingestAndFindWithVectorize(collection, new EmbeddingAPIKeyHeaderProvider( System.getenv("HUGGINGFACEDED_API_KEY")));

        // Drop Collection
        getDatabase().dropCollection(collectionName);
    }

    @Override
    protected String getApiKey() {
        return "";
    }

    @Override
    protected String getEmbeddingProviderId() {
        return "";
    }

    @Override
    protected Map<String, Object> getAuthenticationParameters() {
        return Map.of();
    }
}
