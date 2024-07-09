package com.datastax.astra.test.integration.collection_vectorize;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.DataAPIOptions;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.admin.DatabaseAdmin;
import com.datastax.astra.client.model.CollectionOptions;
import com.datastax.astra.client.model.CommandOptions;
import com.datastax.astra.client.model.DataAPIKeywords;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.EmbeddingProvider;
import com.datastax.astra.client.model.FindEmbeddingProvidersResult;
import com.datastax.astra.client.model.FindOneOptions;
import com.datastax.astra.client.model.InsertManyOptions;
import com.datastax.astra.client.model.InsertManyResult;
import com.datastax.astra.client.model.Projections;
import com.datastax.astra.client.model.SimilarityMetric;
import com.datastax.astra.client.auth.AWSEmbeddingHeadersProvider;
import com.datastax.astra.client.auth.EmbeddingHeadersProvider;
import com.datastax.astra.internal.command.LoggingCommandObserver;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testing Vectorize in DEV
 */
@Slf4j
public class AstraDevVectorizeITTest extends AbstractVectorizeITTest {

    @Override
    protected Database initDatabase() {
        return gcpEuropeWest4();
        //return gcpUsCentral();
        //return azureEastUs();
        //return awsUSWest2();
    }

    private Database awsUSWest2() {
        Database db = initAstraDatabase(AstraEnvironment.DEV, "aws_us-west-2", CloudProviderType.AWS, "us-west-2");
        db.getCommandOptions().getObservers().put("logging", new LoggingCommandObserver(DataAPIClient.class));
        return db;
    }

    private Database gcpUsCentral() {
        Database db = initAstraDatabase(AstraEnvironment.DEV, "gcp_us_central1", CloudProviderType.GCP, "us-central1");
        db.getCommandOptions().getObservers().put("logging", new LoggingCommandObserver(DataAPIClient.class));
        return db;
    }

    private Database gcpEuropeWest4() {
        Database db = initAstraDatabase(AstraEnvironment.DEV, "gcp_europe_west4", CloudProviderType.GCP, "europe-west4");
        db.getCommandOptions().getObservers().put("logging", new LoggingCommandObserver(DataAPIClient.class));
        return db;
    }

    private Database azureEastUs() {
        Database db = initAstraDatabase(AstraEnvironment.DEV, "azure_eastus", CloudProviderType.AZURE, "eastus");
        db.getCommandOptions().getObservers().put("logging", new LoggingCommandObserver(DataAPIClient.class));
        return db;
    }

    @Test
    public void shouldTestAllProviders() {
        for (Map.Entry<String, EmbeddingProvider> entry : getDatabase()
                .getDatabaseAdmin()
                .findEmbeddingProviders()
                .getEmbeddingProviders()
                .entrySet()) {
            //this.testEmbeddingProvider(entry.getKey(), entry.getValue());
            System.out.println("Provider: " + entry.getKey());
        }
    }

    @Test
    public void testOneProvider() {
        dropAllCollections();
        shouldTestOneProvider("openai");
        shouldTestOneProvider("jinaAI");
        shouldTestOneProvider("voyageAI");
        dropAllCollections();
        shouldTestOneProvider("huggingface");
        shouldTestOneProvider("upstageAI");
        shouldTestOneProvider("mistral");
        dropAllCollections();
        shouldTestOneProvider("nvidia");
        shouldTestOneProvider("azureOpenAI");
        dropAllCollections();
        shouldTestOneProviderSharedKey("azureOpenAI", "stefano");
        shouldTestOneProviderSharedKey("huggingface", "HF_API_KEY");
        dropAllCollections();
        shouldTestOneProviderSharedKey("jinaAI", "JINA_API_KEY");
        dropAllCollections();
        shouldTestOneProviderSharedKey("voyageAI", "VOYAGE_API_KEY");
        shouldTestOneProviderSharedKey("upstageAI", "UPSTAGE_API_KEY");
        dropAllCollections();
        shouldTestOneProviderSharedKey("mistral", "MISTRAL_API_KEY");
        shouldTestOneProviderSharedKey("openai", "k1");
    }

    @Test
    public void testHuggingFaceDedicated() {
        Database db = initDatabase();

        String providerName   = "huggingfaceDedicated";
        String huggingFaceKey = "HF_DEDICATED_API_KEY";
        String collectionName = "collection_hf_dedicated";

        FindEmbeddingProvidersResult result =  db.getDatabaseAdmin().findEmbeddingProviders();
        assertThat(result.getEmbeddingProviders().get(providerName)).isNotNull();

        // Create collection for HF Dedicated
        dropAllCollections();
        CollectionOptions.CollectionOptionsBuilder builder = CollectionOptions.builder();
        builder.vectorSimilarity(SimilarityMetric.COSINE);
        builder.vectorDimension(384);
        Map<String, Object > params = new HashMap<>();
        params.put("endpointName", "f70gexilb56vxkhc");
        params.put("regionName", "us-east-1");
        params.put("cloudName", "aws");
        builder.vectorize(providerName, null, huggingFaceKey, params);
        Collection<Document> collection = db.createCollection(collectionName, builder.build(), new CommandOptions<>());
        assertThat(db.collectionExists(collectionName)).isTrue();

        // Insertion With Vectorize
        testCollectionSharedKey(collection);

        /*
        System.out.println("TESTING PROVIDER [" + key + "]");
        Map<String, Object> params = getParameters(key);
        provider.getModels().forEach(model -> {
            System.out.println("Processing MODEL " + model.getName());
            try {
                log.info("Testing model {}", model);

                log.info("Collection created {}", collection.getName());
                testCollectionSharedKey(collection);
                collection.drop();

            } catch(Exception e) {
                log.error("Error while testing model {}", model, e);
            }*/
    }

    @Test
    public void shouldTestAwsBedRock() {
        String token = System.getenv("ASTRA_DB_APPLICATION_TOKEN_DEV");
        EmbeddingHeadersProvider awsAuthProvider = new AWSEmbeddingHeadersProvider(
                System.getenv("BEDROCK_HEADER_AWS_ACCESS_ID"),
                System.getenv("BEDROCK_HEADER_AWS_SECRET_ID")
        );

        DataAPIClient client = new DataAPIClient(token, DataAPIOptions
                .builder()
                .withDestination(DataAPIOptions.DataAPIDestination.ASTRA_DEV)
                .withObserver(new LoggingCommandObserver(AstraDevVectorizeITTest.class))
                .withEmbeddingAuthProvider(awsAuthProvider)
                // <---
                .build());

        DatabaseAdmin databaseAdmin = client.getAdmin().
                createDatabase("gcp_europe_west4", CloudProviderType.GCP, "europe-west4");
        Database db = databaseAdmin.getDatabase();

        //Database db = gcpEuropeWest4();

        String providerName   = "bedrock";
        String providerModel  = "amazon.titan-embed-text-v1";
        String collectionName = "aws_bedrock_titan_v1";
        String awsRegionName  = System.getenv("BEDROCK_HEADER_AWS_REGION");

        // Validate that 'bedrock' is a valid provider
        FindEmbeddingProvidersResult result = databaseAdmin.findEmbeddingProviders();
        assertThat(result).isNotNull();
        assertThat(result.getEmbeddingProviders()).isNotNull();
        assertThat(result.getEmbeddingProviders()).containsKey(providerName);

        // Create collection for AWS Bedrock
        db.dropCollection(collectionName);
        Collection<Document> collection = db.createCollection(collectionName, CollectionOptions
                .builder()
                .vectorize(providerName, providerModel,null,  Map.of("region", awsRegionName))
                .build());;
        assertThat(db.collectionExists(collectionName)).isTrue();
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

        InsertManyResult res = collection.insertMany(entries, new InsertManyOptions());
        assertThat(res.getInsertedIds()).hasSize(8);
        log.info("{} Documents inserted", res.getInsertedIds().size());
        Optional<Document> doc = collection.findOne(null,
                new FindOneOptions()
                        .sort("You shouldn't come around here singing up at people like tha")
                        .projection(Projections.exclude(DataAPIKeywords.VECTOR.getKeyword()))
                        //.embeddingAuthProvider(awsAuthProvider)
                        .includeSimilarity());
        log.info("Document found {}", doc);
        assertThat(doc).isPresent();
        assertThat(doc.get().getId(Integer.class)).isEqualTo(7);
        assertThat(doc.get().getDouble(DataAPIKeywords.SIMILARITY.getKeyword())).isGreaterThan(.8);
    }


}
