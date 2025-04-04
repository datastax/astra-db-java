package dev.langchain4j.store.embedding.astradb;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.admin.AstraDBAdmin;
import com.datastax.astra.client.admin.AstraDBDatabaseAdmin;
import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.langchain4j.store.embedding.AstraDbEmbeddingStore;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiModelName;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIT;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.List;
import java.util.UUID;

import static com.datastax.astra.client.core.vector.SimilarityMetric.COSINE;
import static com.dtsx.astra.sdk.utils.TestUtils.getAstraToken;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Percentage.withPercentage;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnabledIfEnvironmentVariable(named = "ASTRA_DB_APPLICATION_TOKEN", matches = "Astra.*")
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = "sk.*")
@Slf4j
class AstraDbEmbeddingStoreIT extends EmbeddingStoreIT {

    static final String TEST_DB = "test_langchain4j";
    static final String TEST_COLLECTION = "test_collection";
    static AstraDbEmbeddingStore embeddingStore;
    static EmbeddingModel embeddingModel;

    static UUID dbId;
    static DataAPIClient client;
    static AstraDBAdmin astraDBAdmin;
    static Database db;

    @BeforeAll
    static void initStoreForTests() {

        /*
         * Token Value is retrieved from environment Variable 'ASTRA_DB_APPLICATION_TOKEN', it should
         * have Organization Administration permissions (to create db)
         */
        client       = new DataAPIClient(getAstraToken());
        astraDBAdmin = client.getAdmin();

        /*
         * Will create a Database in Astra with the name 'test_langchain4j' if does not exist and work
         * with its identifier. The call is blocking and will wait until the database is ready.
         */
        AstraDBDatabaseAdmin databaseAdmin = (AstraDBDatabaseAdmin) astraDBAdmin.createDatabase(TEST_DB);
        dbId = UUID.fromString(databaseAdmin.getDatabaseInformations().getId());
        assertThat(dbId).isNotNull();
        log.info("[init] - Database exists id={}", dbId);

        /*
         * Initialize the client from the database identifier. A database will host multiple collections.
         * A collection stands for an Embedding Store.
         */
        db = databaseAdmin.getDatabase();
        Assertions.assertThat(db).isNotNull();

        // Select Collection
        CollectionDefinition cd = new CollectionDefinition()
                .vectorDimension(1536)
                .vectorSimilarity(COSINE);
        Collection<Document> collection = db.createCollection(TEST_COLLECTION, cd);
        Assertions.assertThat(collection).isNotNull();
        collection.deleteAll();
        log.info("[init] - Collection create name={}", TEST_COLLECTION);

        // Creating the store (and collection) if not exists
        embeddingStore = new AstraDbEmbeddingStore(collection);
        log.info("[init] - Embedding Store initialized");
    }

    @Override
    protected void clearStore() {
        embeddingStore.clear();
    }

    @Override
    protected EmbeddingStore<TextSegment> embeddingStore() {
        return embeddingStore;
    }

    @Override
    protected EmbeddingModel embeddingModel() {
        if (embeddingModel == null) {
            embeddingModel = OpenAiEmbeddingModel.builder()
                    .apiKey(System.getenv("OPENAI_API_KEY"))
                    .modelName(OpenAiModelName.TEXT_EMBEDDING_ADA_002)
                    .build();
        }
        return embeddingModel;
    }

    @Test
    void testAddEmbeddingAndFindRelevant() {
        CollectionDefinition collectionDefinition = new CollectionDefinition()
                .vectorDimension(11)
                .vectorSimilarity(COSINE);
        Collection<Document> smallCollection = db.createCollection("SMALL", collectionDefinition);
        EmbeddingStore<TextSegment> smallStore = new AstraDbEmbeddingStore(smallCollection);

        Embedding embedding = Embedding.from(new float[]{9.9F, 4.5F, 3.5F, 1.3F, 1.7F, 5.7F, 6.4F, 5.5F, 8.2F, 9.3F, 1.5F});
        TextSegment textSegment = TextSegment.from("Text", Metadata.from("Key", "Value"));
        String id = smallStore.add(embedding, textSegment);
        assertThat(id != null && !id.isEmpty()).isTrue();

        Embedding referenceEmbedding = Embedding.from(new float[]{8.7F, 4.5F, 3.4F, 1.2F, 5.5F, 5.6F, 6.4F, 5.5F, 8.1F, 9.1F, 1.1F});
        List<EmbeddingMatch<TextSegment>> embeddingMatches = smallStore.findRelevant(referenceEmbedding, 1);
        assertThat(embeddingMatches).hasSize(1);

        EmbeddingMatch<TextSegment> embeddingMatch = embeddingMatches.get(0);
        assertThat(embeddingMatch.score()).isBetween(0d, 1d);
        assertThat(embeddingMatch.embeddingId()).isEqualTo(id);
        assertThat(embeddingMatch.embedding()).isEqualTo(embedding);
        assertThat(embeddingMatch.embedded()).isEqualTo(textSegment);

        db.dropCollection("SMALL");
    }

    /**
     * OVERRIDING FROM DEFAULT TEST AS  THE UUID are ENCODED AS {"$uuid":"value"} in ASTRA
     */
    @Test
    void should_add_embedding_with_segment_with_metadata() throws InterruptedException {

        Metadata metadata = createMetadata();

        TextSegment segment = TextSegment.from("hello", metadata);
        Embedding embedding = embeddingModel().embed(segment.text()).content();

        String id = embeddingStore().add(embedding, segment);
        assertThat(id).isNotBlank();

        TextSegment altSegment = TextSegment.from("hello?");
        Embedding altEmbedding = embeddingModel().embed(altSegment.text()).content();
        embeddingStore().add(altEmbedding, altSegment);

        Thread.sleep(1000);
        List<EmbeddingMatch<TextSegment>> relevant = embeddingStore().findRelevant(embedding, 1);
        assertThat(relevant).hasSize(1);

        EmbeddingMatch<TextSegment> match = relevant.get(0);
        assertThat(match.score()).isCloseTo(1, withPercentage(1));
        assertThat(match.embeddingId()).isEqualTo(id);
        assertThat(match.embedding()).isEqualTo(embedding);

        assertThat(match.embedded().text()).isEqualTo(segment.text());

        assertThat(match.embedded().metadata().getString("string_empty")).isEqualTo("");
        assertThat(match.embedded().metadata().getString("string_space")).isEqualTo(" ");
        assertThat(match.embedded().metadata().getString("string_abc")).isEqualTo("abc");
        assertThat(match.embedded().metadata().getInteger("integer_min")).isEqualTo(Integer.MIN_VALUE);
        assertThat(match.embedded().metadata().getInteger("integer_minus_1")).isEqualTo(-1);
        assertThat(match.embedded().metadata().getInteger("integer_0")).isEqualTo(0);
        assertThat(match.embedded().metadata().getInteger("integer_1")).isEqualTo(1);
        assertThat(match.embedded().metadata().getInteger("integer_max")).isEqualTo(Integer.MAX_VALUE);
        assertThat(match.embedded().metadata().getLong("long_min")).isEqualTo(Long.MIN_VALUE);
        assertThat(match.embedded().metadata().getLong("long_minus_1")).isEqualTo(-1L);
        assertThat(match.embedded().metadata().getLong("long_0")).isEqualTo(0L);
        assertThat(match.embedded().metadata().getLong("long_1")).isEqualTo(1L);
        assertThat(match.embedded().metadata().getLong("long_max")).isEqualTo(Long.MAX_VALUE);

        assertThat(match.embedded().metadata().getFloat("float_min")).isEqualTo(-Float.MAX_VALUE);
        assertThat(match.embedded().metadata().getFloat("float_minus_1")).isEqualTo(-1f);
        assertThat(match.embedded().metadata().getFloat("float_0")).isEqualTo(Float.MIN_VALUE);
        assertThat(match.embedded().metadata().getFloat("float_1")).isEqualTo(1f);
        assertThat(match.embedded().metadata().getFloat("float_123")).isEqualTo(1.23456789f);
        assertThat(match.embedded().metadata().getFloat("float_max")).isEqualTo(Float.MAX_VALUE);

        assertThat(match.embedded().metadata().getDouble("double_minus_1")).isEqualTo(-1d);
        assertThat(match.embedded().metadata().getDouble("double_0")).isEqualTo(Double.MIN_VALUE);
        assertThat(match.embedded().metadata().getDouble("double_1")).isEqualTo(1d);
        assertThat(match.embedded().metadata().getDouble("double_123")).isEqualTo(1.23456789d);

        // new API
        assertThat(embeddingStore().search(EmbeddingSearchRequest.builder()
                .queryEmbedding(embedding)
                .maxResults(1)
                .build()).matches()).isEqualTo(relevant);
    }



}
