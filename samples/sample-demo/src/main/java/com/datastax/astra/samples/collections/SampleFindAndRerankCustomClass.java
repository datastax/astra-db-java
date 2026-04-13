package com.datastax.astra.samples.collections;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.commands.cursor.CollectionFindAndRerankCursor;
import com.datastax.astra.client.collections.commands.options.CollectionFindAndRerankOptions;
import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.hybrid.Hybrid;
import com.datastax.astra.client.core.lexical.Analyzer;
import com.datastax.astra.client.core.lexical.LexicalOptions;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Projection;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.rerank.CollectionRerankOptions;
import com.datastax.astra.client.core.rerank.RerankServiceOptions;
import com.datastax.astra.client.core.rerank.RerankedResult;
import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.datastax.astra.client.core.vector.VectorOptions;
import com.datastax.astra.client.core.vectorize.VectorServiceOptions;
import com.datastax.astra.client.databases.Database;
import com.dtsx.astra.sdk.AstraOpsClient;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.datastax.astra.client.core.lexical.AnalyzerTypes.STANDARD;

/**
 * Demonstrates {@code findAndRerank} with {@link Hybrid}, projection to custom POJO,
 * {@code rerankOn("$lexical")}, and {@code rerankQuery()}.
 *
 * @see Collection#findAndRerank(Filter, CollectionFindAndRerankOptions, Class)
 * @see Hybrid
 */
@SuppressWarnings("unused")
public class SampleFindAndRerankCustomClass {

    static final String COLLECTION_FIND_RERANK = "c_find_rerank";
    static final String ASTRA_TOKEN = System.getenv("ASTRA_DB_APPLICATION_TOKEN");
    static final String ASTRA_ENDPOINT = "https://cad896f4-49e4-4b55-bce3-36dd290e4b9b-us-east-2.apps.astra.datastax.com";

    @Data
    public static class Projected {
        boolean is_checked_out;
        String title;
    }

    @Data
    public static class Book {
        boolean is_checked_out;
        String title;
        String author;
        String description;
        @JsonProperty("_id")
        String id;
        @JsonProperty("$vectorize")
        String vectorize;
        @JsonProperty("$lexical")
        String lexical;
    }

    public static void main(String[] args) {
        // Connection
        DataAPIClientOptions options = new DataAPIClientOptions().logRequests();
        Database db = new DataAPIClient(ASTRA_TOKEN, options).getDatabase(ASTRA_ENDPOINT);
        System.out.println("Connected to Astra db:" + db.getInfo().getName());

        // Access Collection
        Collection<Book> c_farr = db.collectionExists(COLLECTION_FIND_RERANK) ?
                db.getCollection(COLLECTION_FIND_RERANK, Book.class) :
                createCollectionFarr(db);
        System.out.println("Creation created");

        c_farr.insertMany(loadBooksFromCsv());
        System.out.println("Data created");

        // Searching collection
        // Find documents
        Filter filter = null;
        CollectionFindAndRerankCursor<Book, Projected> cursor =
                c_farr.findAndRerank(
                        filter,
                        new CollectionFindAndRerankOptions()
                                .projection(Projection.include("is_checked_out", "title"))
                                .sort(Sort.hybrid("A tree in the woods")),
                        Projected.class);

        // Iterate over the results
        for (RerankedResult<Projected> result : cursor) {
            System.out.println(result.getDocument());
        }
    }

    public static Collection<Book> createCollectionFarr(Database db) {
        VectorServiceOptions vectorService = new VectorServiceOptions()
                .provider("nvidia")
                .modelName("NV-Embed-QA");
        VectorOptions vectorOptions = new VectorOptions()
                .dimension(1024)
                .metric(SimilarityMetric.COSINE.getValue())
                .service(vectorService);

        LexicalOptions lexicalOptions = new LexicalOptions()
                .enabled(true)
                .analyzer(new Analyzer(STANDARD));
        RerankServiceOptions rerankService = new RerankServiceOptions()
                .modelName("nvidia/llama-3.2-nv-rerankqa-1b-v2")
                .provider("nvidia");
        CollectionRerankOptions rerankOptions = new CollectionRerankOptions()
                .enabled(true)
                .service(rerankService);

       return db.createCollection(COLLECTION_FIND_RERANK, new CollectionDefinition()
                .vector(vectorOptions)
                .lexical(lexicalOptions)
                .rerank(rerankOptions), Book.class);
    }

    public static List<Book> loadBooksFromCsv() {
        List<Book> books = new ArrayList<>();
        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(
                        SampleFindAndRerankCustomClass.class.getClassLoader()
                                .getResourceAsStream("books.csv")))) {
            String line;
            boolean isHeader = true;
            int count = 0;
            while ((line = reader.readLine()) != null) {
                // Skip header line
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                // Parse CSV line (simple parsing, assumes no commas in fields)
                String[] fields = line.split(",", 4);
                if (fields.length == 4) {
                    Book book = new Book();
                    book.set_checked_out(Boolean.parseBoolean(fields[0]));
                    book.setTitle(fields[1]);
                    book.setAuthor(fields[2]);
                    book.setDescription(fields[3]);
                    book.setId(String.valueOf(count));
                    book.setVectorize(book.getDescription());
                    book.setLexical(book.getTitle());
                    books.add(book);
                    count++;
                }
            }
            System.out.println("Loaded " + count + " books from CSV");
        } catch (Exception e) {
            System.err.println("Error loading books from CSV: " + e.getMessage());
        }
        return books;
    }
}
