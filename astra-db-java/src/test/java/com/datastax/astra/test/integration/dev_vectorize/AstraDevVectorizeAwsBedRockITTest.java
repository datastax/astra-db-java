package com.datastax.astra.test.integration.dev_vectorize;

import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.CollectionOptions;
import com.datastax.astra.client.collections.documents.Document;
import com.datastax.astra.client.collections.options.CollectionFindOneOptions;
import com.datastax.astra.client.collections.options.CollectionInsertManyOptions;
import com.datastax.astra.client.collections.results.CollectionInsertManyResult;
import com.datastax.astra.client.core.auth.AWSEmbeddingHeadersProvider;
import com.datastax.astra.client.core.auth.EmbeddingHeadersProvider;
import com.datastax.astra.client.core.query.Projection;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.results.FindEmbeddingProvidersResult;
import com.datastax.astra.client.core.types.DataAPIKeywords;
import com.datastax.astra.test.integration.AbstractVectorizeITTest;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@EnabledIfEnvironmentVariable(named = "ASTRA_DB_APPLICATION_TOKEN_DEV", matches = "Astra.*")
@EnabledIfEnvironmentVariable(named = "ASTRA_CLOUD_PROVIDER_DEV", matches = ".*")
@EnabledIfEnvironmentVariable(named = "ASTRA_CLOUD_REGION_DEV", matches = ".*")
@EnabledIfEnvironmentVariable(named = "EMBEDDING_PROVIDER", matches = ".*")
@EnabledIfEnvironmentVariable(named = "BEDROCK_HEADER_AWS_ACCESS_ID", matches = ".*")
@EnabledIfEnvironmentVariable(named = "BEDROCK_HEADER_AWS_SECRET_ID", matches = ".*")
@EnabledIfEnvironmentVariable(named = "BEDROCK_REGION", matches = ".*")
public class AstraDevVectorizeAwsBedRockITTest extends AbstractVectorizeITTest {

    @Override
    public AstraEnvironment getAstraEnvironment() {
        return AstraEnvironment.DEV;
    }

    @Override
    public CloudProviderType getCloudProvider() {
        return CloudProviderType.valueOf(System.getenv("ASTRA_CLOUD_PROVIDER_DEV"));
    }

    @Override
    public String getRegion() {
        return System.getenv("ASTRA_CLOUD_REGION_DEV");
    }

    @Test
    public void shouldTestAwsBedRock() {
        String token = System.getenv("ASTRA_DB_APPLICATION_TOKEN_DEV");
        EmbeddingHeadersProvider awsAuthProvider = new AWSEmbeddingHeadersProvider(
                System.getenv("BEDROCK_HEADER_AWS_ACCESS_ID"),
                System.getenv("BEDROCK_HEADER_AWS_SECRET_ID")
        );

        String providerName   = "bedrock";
        String providerModel  = "amazon.titan-embed-text-v1";
        String collectionName = "aws_bedrock_titan_v1";

        // Validate that 'bedrock' is a valid provider
        FindEmbeddingProvidersResult result = getDatabaseAdmin().findEmbeddingProviders();
        assertThat(result).isNotNull();
        assertThat(result.getEmbeddingProviders()).isNotNull();
        assertThat(result.getEmbeddingProviders()).containsKey(providerName);

        // Create collection for AWS Bedrock
        Collection<Document> collection = getDatabase().createCollection(collectionName, CollectionOptions
                .builder()
                .vectorize(providerName, providerModel,null,
                        Map.of("region", System.getenv("BEDROCK_REGION")))
                .build());;
        assertThat(getDatabase().collectionExists(collectionName)).isTrue();
        // Insertion With Vectorize
        List<Document> entries = List.of(
                new Document(1).vectorize("A lovestruck Romeo sings the streets a serenade"),
                new Document(2).vectorize("Finds a streetlight, steps out of the shade"),
                new Document(3).vectorize("Says something like, You and me babe, how about it?"),
                new Document(4).vectorize("Juliet says,Hey, it's Romeo, you nearly gimme a heart attack"),
                new Document(5).vectorize("He's underneath the window"),
                new Document(6).vectorize("She's singing, Hey la, my boyfriend's back"),
                new Document(7).vectorize("You shouldn't come around here singing up at people like that"),
                new Document(8).vectorize("Anyway, what you gonna do about it?")
        );

        CollectionInsertManyResult res = collection.insertMany(entries, new CollectionInsertManyOptions().embeddingAuthProvider(awsAuthProvider));
        assertThat(res.getInsertedIds()).hasSize(8);
        log.info("{} Documents inserted", res.getInsertedIds().size());
        Optional<Document> doc = collection.findOne(null,
                new CollectionFindOneOptions()
                        .sort(Sort.vectorize("You shouldn't come around here singing up at people like tha"))
                        .projection(Projection.exclude(DataAPIKeywords.VECTOR.getKeyword()))
                        .embeddingAuthProvider(awsAuthProvider)
                        .includeSimilarity(true));
        log.info("Document found {}", doc);
        assertThat(doc).isPresent();
        assertThat(doc.get().getId(Integer.class)).isEqualTo(7);
        assertThat(doc.get().getDouble(DataAPIKeywords.SIMILARITY.getKeyword())).isGreaterThan(.8);
    }

}
