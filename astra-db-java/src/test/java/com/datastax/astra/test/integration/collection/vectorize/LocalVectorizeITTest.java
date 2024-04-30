package com.datastax.astra.test.integration.collection.vectorize;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.model.DataAPIKeywords;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.InsertManyResult;
import com.datastax.astra.client.model.Projections;
import com.datastax.astra.internal.command.LoggingCommandObserver;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static com.datastax.astra.client.model.FindOneOptions.Builder.sort;
import static com.datastax.astra.client.model.InsertManyOptions.Builder.embeddingServiceApiKey;
import static com.datastax.astra.test.integration.collection.vectorize.EmbeddingModel.COHERE_EMBED_ENGLISH_V2;
import static com.datastax.astra.test.integration.collection.vectorize.EmbeddingModel.COHERE_EMBED_ENGLISH_V3;
import static com.datastax.astra.test.integration.collection.vectorize.EmbeddingModel.HF_MINI_LM_L6;
import static com.datastax.astra.test.integration.collection.vectorize.EmbeddingModel.JINA_AI_EMBEDDINGS_V2_CODE;
import static com.datastax.astra.test.integration.collection.vectorize.EmbeddingModel.JINA_AI_EMBEDDINGS_V2_DE;
import static com.datastax.astra.test.integration.collection.vectorize.EmbeddingModel.JINA_AI_EMBEDDINGS_V2_EN;
import static com.datastax.astra.test.integration.collection.vectorize.EmbeddingModel.JINA_AI_EMBEDDINGS_V2_ES;
import static com.datastax.astra.test.integration.collection.vectorize.EmbeddingModel.JINA_AI_EMBEDDINGS_V2_ZH;
import static com.datastax.astra.test.integration.collection.vectorize.EmbeddingModel.MISTRAL_AI;
import static com.datastax.astra.test.integration.collection.vectorize.EmbeddingModel.OPENAI_3_LARGE;
import static com.datastax.astra.test.integration.collection.vectorize.EmbeddingModel.OPENAI_3_SMALL;
import static com.datastax.astra.test.integration.collection.vectorize.EmbeddingModel.OPENAI_ADA002;
import static com.datastax.astra.test.integration.collection.vectorize.EmbeddingModel.UPSTAGE_AI_SOLAR_MINI_1_PASSAGE;
import static com.datastax.astra.test.integration.collection.vectorize.EmbeddingModel.UPSTAGE_AI_SOLAR_MINI_1_QUERY;
import static com.datastax.astra.test.integration.collection.vectorize.EmbeddingModel.VERTEX_AI_GECKO_003;
import static com.datastax.astra.test.integration.collection.vectorize.EmbeddingModel.VOYAGE_AI_2;
import static com.datastax.astra.test.integration.collection.vectorize.EmbeddingModel.VOYAGE_AI_CODE_2;
import static com.datastax.astra.test.integration.collection.vectorize.EmbeddingModel.VOYAGE_AI_LARGE_2;
import static com.datastax.astra.test.integration.collection.vectorize.EmbeddingModel.VOYAGE_AI_LAW_2;
import static com.datastax.astra.test.integration.collection.vectorize.EmbeddingModel.VOYAGE_AI_LITE_INSTRUCT;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class LocalVectorizeITTest extends AbstractVectorizeITTest {

    @Override
    protected Database initDatabase() {
        return DataAPIClients.createDefaultLocalDatabase();
    }

    @Test
    public void shouldTestEmbeddingsOpenAI() {
        testEmbeddingModel(OPENAI_ADA002, System.getenv("OPENAI_API_KEY"));
        testEmbeddingModel(OPENAI_3_SMALL, System.getenv("OPENAI_API_KEY"));
        testEmbeddingModel(OPENAI_3_LARGE, System.getenv("OPENAI_API_KEY"));
        dropCollection(OPENAI_ADA002);
        dropCollection(OPENAI_3_SMALL);
        dropCollection(OPENAI_3_LARGE);
    }

    @Test
    public void shouldTestEmbeddingsHuggingFace() {
        dropAllCollections();
        testEmbeddingModel(HF_MINI_LM_L6, System.getenv("HF_API_KEY"));
        dropCollection(HF_MINI_LM_L6);
    }

    @Test
    public void shouldTestEmbeddingsCohere() {
        dropAllCollections();
        testEmbeddingModel(COHERE_EMBED_ENGLISH_V2, System.getenv("COHERE_API_KEY"));
        testEmbeddingModel(COHERE_EMBED_ENGLISH_V3, System.getenv("COHERE_API_KEY"));
        dropCollection(COHERE_EMBED_ENGLISH_V2);
        dropCollection(COHERE_EMBED_ENGLISH_V3);
    }

    @Test
    @Disabled("NOT WORKING")
    public void shouldTestEmbeddingsVertexAI() {
        dropAllCollections();
        Collection<Document> collection = createCollectionVertex(VERTEX_AI_GECKO_003, "devoxxfrance");
        testCollection(collection, "");
        dropCollection(VERTEX_AI_GECKO_003);
    }

    @Test
    @Disabled("NOT READY")
    public void shouldTestVoyageAI() {
        dropAllCollections();
        testEmbeddingModel(VOYAGE_AI_2, System.getenv("VOYAGE_API_KEY"));
        testEmbeddingModel(VOYAGE_AI_LAW_2, System.getenv("VOYAGE_API_KEY"));
        testEmbeddingModel(VOYAGE_AI_CODE_2, System.getenv("VOYAGE_API_KEY"));
        testEmbeddingModel(VOYAGE_AI_LARGE_2, System.getenv("VOYAGE_API_KEY"));
        testEmbeddingModel(VOYAGE_AI_LITE_INSTRUCT, System.getenv("VOYAGE_API_KEY"));
        dropCollection(VOYAGE_AI_2);
        dropCollection(VOYAGE_AI_LAW_2);
        dropCollection(VOYAGE_AI_CODE_2);
        dropCollection(VOYAGE_AI_LARGE_2);
        dropCollection(VOYAGE_AI_LITE_INSTRUCT);
    }

    @Test
    @Disabled("NOT READY")
    public void shouldTestJina() {
        dropAllCollections();
        testEmbeddingModel(JINA_AI_EMBEDDINGS_V2_EN, System.getenv("JINA_API_KEY"));
        testEmbeddingModel(JINA_AI_EMBEDDINGS_V2_DE, System.getenv("JINA_API_KEY"));
        testEmbeddingModel(JINA_AI_EMBEDDINGS_V2_ES, System.getenv("JINA_API_KEY"));
        testEmbeddingModel(JINA_AI_EMBEDDINGS_V2_ZH, System.getenv("JINA_API_KEY"));
        testEmbeddingModel(JINA_AI_EMBEDDINGS_V2_CODE, System.getenv("JINA_API_KEY"));
        dropCollection(JINA_AI_EMBEDDINGS_V2_EN);
        dropCollection(JINA_AI_EMBEDDINGS_V2_DE);
        dropCollection(JINA_AI_EMBEDDINGS_V2_ES);
        dropCollection(JINA_AI_EMBEDDINGS_V2_ZH);
        dropCollection(JINA_AI_EMBEDDINGS_V2_CODE);
    }

    @Test
    @Disabled("NOT READY")
    public void shouldTestMistralAI() {
        dropAllCollections();
        testEmbeddingModel(MISTRAL_AI, System.getenv("MISTRAL_API_KEY"));
        dropCollection(MISTRAL_AI);
    }

    @Test
    @Disabled("NOT READY")
    public void shouldTestUpstageAI() {
        dropAllCollections();
        testEmbeddingModel(UPSTAGE_AI_SOLAR_MINI_1_QUERY, System.getenv("UPSTAGE_API_KEY"));
        testEmbeddingModel(UPSTAGE_AI_SOLAR_MINI_1_PASSAGE, System.getenv("UPSTAGE_API_KEY"));
        dropCollection(UPSTAGE_AI_SOLAR_MINI_1_QUERY);
        dropCollection(UPSTAGE_AI_SOLAR_MINI_1_PASSAGE);
    }

    private void testEmbeddingModel(EmbeddingModel model, String apiKey) {
        log.info("Testing model {}", model);
        Collection<Document> collection = createCollection(model);
        collection.registerListener("logger", new LoggingCommandObserver(LocalVectorizeITTest.class));
        log.info("Collection created {}", collection.getName());
        testCollection(collection, apiKey);
    }

    private void testCollection(Collection<Document> collection, String apiKey) {
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

        // Ingestion
        InsertManyResult res = collection.insertMany(entries, embeddingServiceApiKey(apiKey));
        assertThat(res.getInsertedIds()).hasSize(8);
        log.info("{} Documents inserted", res.getInsertedIds().size());
        Optional<Document> doc = collection.findOne(null,
                sort("You shouldn't come around here singing up at people like tha")
                        .projection(Projections.exclude(DataAPIKeywords.VECTOR.getKeyword()))
                        .embeddingServiceApiKey(apiKey)
                        .includeSimilarity());
        log.info("Document found {}", doc);
        assertThat(doc).isPresent();
        assertThat(doc.get().getId(Integer.class)).isEqualTo(7);
        assertThat(doc.get().getDouble(DataAPIKeywords.SIMILARITY.getKeyword())).isGreaterThan(.8);
    }

}
