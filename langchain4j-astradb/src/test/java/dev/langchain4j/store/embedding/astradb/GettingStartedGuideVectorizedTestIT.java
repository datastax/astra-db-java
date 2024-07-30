package dev.langchain4j.store.embedding.astradb;

import com.datastax.astra.client.Database;
import com.datastax.astra.client.model.CollectionOptions;
import com.datastax.astra.client.model.SimilarityMetric;
import com.datastax.astra.langchain4j.AstraDBTestSupport;
import com.datastax.astra.langchain4j.rag.AstraVectorizeIngestor;
import com.datastax.astra.langchain4j.store.embedding.AstraDbEmbeddingStore;
import com.datastax.astra.langchain4j.store.embedding.EmbeddingSearchRequestAstra;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * ASTRA can now compute the embeddings for you. This is a simple example of how to use ASTRA to compute embeddings.
 */
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnabledIfEnvironmentVariable(named = "ASTRA_DB_APPLICATION_TOKEN", matches = "Astra.*")
public class GettingStartedGuideVectorizedTestIT {

    /**
     * Different Embedding Stores topologies
     */
    static Database astraDatabase;
    static AstraDbEmbeddingStore embeddingStoreVectorizeNVidia;

    @BeforeAll
    public static void initStoreForTests() {
        astraDatabase = AstraDBTestSupport.createDbIfNotExist(AstraDBTestSupport.TEST_DB_vectorize);

        /*
         * An embedding store that compute the embedding for you on the fly without
         * the need of a embedding model. It is done at database level for you.
         */
        embeddingStoreVectorizeNVidia = new AstraDbEmbeddingStore(
                astraDatabase.createCollection("store_with_nvidia", CollectionOptions
                        .builder()
                        .vector(1024, SimilarityMetric.COSINE)
                        .vectorize("nvidia", "NV-Embed-QA")
                        .build()));
        embeddingStoreVectorizeNVidia.clear();
    }

    private static void ingestDocument(String docName, AstraDbEmbeddingStore store) {

        URL url = AstraDBTestSupport.class.getResource("/" + docName);
        Path path = new File(url.getFile()).toPath();

        AstraVectorizeIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(300, 0))
                .embeddingStore(store).build()
                .ingest(FileSystemDocumentLoader.loadDocument(path, new TextDocumentParser()));
    }

    @Test
    @Order(1)
    public void should_ingest_documents() {
        ingestDocument("johnny.txt", embeddingStoreVectorizeNVidia);
    }

    @Test
    @Order(2)
    public void should_search_results() {
        String question = "Who is Johnny?";

        /* RAG
         * I needed to create a new object EmbeddingSearchRequestAstra
         * to add the field "vectorize" to the search.
         *
         * "Vectorize" (nvidia nemo) will not work with content retriever.
         * For advanced RAG. I suggest to keep EmbeddingModel End embedding Store separated.
         */

        EmbeddingSearchRequestAstra searchQuery = new EmbeddingSearchRequestAstra(null,
                question, 10, 0.5, metadataKey("documentId")
                .isIn("f47ac10b-58cc-4372-a567-0e02b2c3d479"));
        List<EmbeddingMatch<TextSegment>> relevantEmbeddings = embeddingStoreVectorizeNVidia.search(searchQuery).matches();
        assertThat(relevantEmbeddings).isNotEmpty();
        assertThat(relevantEmbeddings).isNotEmpty();

        relevantEmbeddings.forEach(match -> log.info("Match: {}", match.embedded()));

        String ragContext = relevantEmbeddings.stream()
                .map(match -> match.embedded().text())
                .collect(Collectors.joining("\n\n"));

        System.out.println(ragContext);
    }
}
