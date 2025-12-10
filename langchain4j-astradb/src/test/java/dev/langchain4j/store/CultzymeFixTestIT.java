package dev.langchain4j.store;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.langchain4j.store.embedding.AstraDbEmbeddingStore;
import com.datastax.astra.langchain4j.store.embedding.EmbeddingSearchRequestAstra;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnabledIfEnvironmentVariable(named = "ASTRA_DB_APPLICATION_TOKEN", matches = "Astra.*")
public class CultzymeFixTestIT {

    public static final String ASTRA_TOKEN    = System.getenv("ASTRA_DB_APPLICATION_TOKEN");
    public static final String ASTRA_ENDPOINT = "https://844c28ee-7824-40a5-9b48-f4ca160c2b7b-us-east-2.apps.astra.datastax.com";
    public static final String OPENAI_API_KEY = System.getenv("OPENAI_API_KEY");
    public static final String COL_OPENAI     = "c_ui_ingest";

    @Test
    public void shouldReadDataFormIngestedPdf() {
        Database db = new DataAPIClient(ASTRA_TOKEN, new DataAPIClientOptions()
                .logRequests()).getDatabase(ASTRA_ENDPOINT);

        /*
        Collection<Document> col = db.createCollection("lc4j", new CollectionDefinition()
                .vector(1024, SimilarityMetric.COSINE)
                .vectorize("nvidia", "NV-Embed-QA"));
        */

        Collection<Document> col = db.getCollection("lc4j");
        AstraDbEmbeddingStore vStore = new AstraDbEmbeddingStore(col);

        List<EmbeddingMatch<TextSegment>> relevantEmbeddings = vStore
                .search(EmbeddingSearchRequestAstra.builderAstra()
                        .queryVectorize("What if ff4j")
                        .maxResults(10)
                        .minScore(0.5)
                        .build()).matches();
        relevantEmbeddings.forEach(match -> log.info("Match: {}", match.embedded()));
        String ragContext = relevantEmbeddings.stream()
                .map(match -> match.embedded().text())
                .collect(Collectors.joining("\n\n"));
        log.info("Rag Context {}", ragContext);

    }
}
