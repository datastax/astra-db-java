package com.datastax.astra.test.integration;

/*-
 * #%L
 * Data API Java Client
 * --
 * Copyright (C) 2024 DataStax
 * --
 * Licensed under the Apache License, Version 2.0
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.commands.options.CollectionFindAndRerankOptions;
import com.datastax.astra.client.collections.commands.options.CollectionFindOptions;
import com.datastax.astra.client.collections.commands.options.CollectionInsertManyOptions;
import com.datastax.astra.client.collections.commands.options.CollectionReplaceOneOptions;
import com.datastax.astra.client.collections.commands.results.CollectionInsertManyResult;
import com.datastax.astra.client.collections.commands.results.CollectionUpdateResult;
import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.DataAPIKeywords;
import com.datastax.astra.client.core.headers.RerankingAPIKeyHeaderProvider;
import com.datastax.astra.client.core.headers.RerankingHeadersProvider;
import com.datastax.astra.client.core.hybrid.Hybrid;
import com.datastax.astra.client.core.lexical.Analyzer;
import com.datastax.astra.client.core.lexical.LexicalOptions;
import com.datastax.astra.client.core.query.Filters;
import com.datastax.astra.client.core.query.Projection;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.rerank.CollectionRerankOptions;
import com.datastax.astra.client.core.rerank.RerankServiceOptions;
import com.datastax.astra.client.core.rerank.RerankedResult;
import com.datastax.astra.client.core.vector.DataAPIVector;
import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.datastax.astra.client.core.vectorize.VectorServiceOptions;
import com.datastax.astra.client.core.vector.VectorOptions;
import com.datastax.astra.client.collections.commands.cursor.CollectionFindAndRerankCursor;
import com.datastax.astra.client.databases.commands.results.FindRerankingProvidersResult;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.datastax.astra.client.core.lexical.AnalyzerTypes.KEYWORD;
import static com.datastax.astra.client.core.lexical.AnalyzerTypes.LETTER;
import static com.datastax.astra.client.core.lexical.AnalyzerTypes.STANDARD;
import static com.datastax.astra.client.core.lexical.AnalyzerTypes.WHITESPACE;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Abstract integration tests for findAndRerank, lexical search, and hybrid search.
 * <p>
 * Uses Nvidia's built-in embedding model ({@code NV-Embed-QA}) which does not require
 * an external API key. The reranking service (Nvidia) uses the Astra token.
 * Subclasses must provide the reranking API key via {@link #getRerankingApiKey()}.
 */
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractCollectionFindAndRerankIT extends AbstractDataAPITest {

    static final String COLLECTION_FIND_RERANK = "c_find_rerank";
    static final String COLLECTION_LEXICAL     = "c_lexical_standard";

    /**
     * @return API key for the reranking service (e.g. Astra token for Nvidia reranking).
     */
    protected abstract String getRerankingApiKey();

    /** Helper — skip test if reranking key is missing. */
    protected boolean skipIfNoRerankingKey() {
        if (getRerankingApiKey() == null || getRerankingApiKey().isBlank()) {
            log.info("Skipping test — Reranking API key not configured");
            return true;
        }
        return false;
    }

    /** Reranking auth header provider. */
    protected RerankingHeadersProvider getRerankingAuthProvider() {
        return new RerankingAPIKeyHeaderProvider(getRerankingApiKey());
    }

    /** Build a base {@link CollectionFindAndRerankOptions} with reranking auth and scores enabled. */
    protected CollectionFindAndRerankOptions baseFindAndRerankOptions() {
        return new CollectionFindAndRerankOptions()
                .rerankingAuthProvider(getRerankingAuthProvider())
                .includeScores(true)
                .limit(10);
    }

    @BeforeAll
    void setupFindAndRerank() {
        dropAllCollections();
        dropAllTables();
    }

    // ========== Reranking providers discovery ==========

    @Test
    @Order(1)
    void should_findRerankingProviders_returnProviders() {
        FindRerankingProvidersResult res = getDatabase()
                .getDatabaseAdmin().findRerankingProviders();
        assertThat(res).isNotNull();
        assertThat(res.getRerankingProviders()).isNotEmpty();
        res.getRerankingProviders().forEach((provider, info) ->
                log.info("Reranking provider: {} — {} model(s)", provider, info.getModels().size()));
    }

    // ========== Collection creation — lexical ==========

    @Test
    @Order(2)
    void should_createCollection_withLexicalStandard() {
        getDatabase().dropCollection(COLLECTION_LEXICAL);
        Collection<Document> col = getDatabase().createCollection(COLLECTION_LEXICAL,
                new CollectionDefinition().lexical(
                        new LexicalOptions().enabled(true).analyzer(new Analyzer(STANDARD))));
        assertThat(col).isNotNull();
        assertThat(col.getDefinition().getLexical()).isNotNull();
    }

    @Test
    @Order(3)
    void should_createCollection_withLexicalCustomTokenizer() {
        String name = "c_lexical_custom";
        getDatabase().dropCollection(name);
        Collection<Document> col = getDatabase().createCollection(name,
                new CollectionDefinition().lexical(
                        new LexicalOptions().analyzer(
                                new Analyzer().tokenizer(WHITESPACE.getValue()))));
        assertThat(col).isNotNull();
    }

    @Test
    @Order(4)
    void should_createCollection_withLexicalKeywordAndSynonym() {
        String name = "c_lexical_keyword";
        getDatabase().dropCollection(name);
        Collection<Document> col = getDatabase().createCollection(name,
                new CollectionDefinition().lexical(
                        new LexicalOptions().analyzer(
                                new Analyzer()
                                        .tokenizer(KEYWORD.getValue())
                                        .addFilter("synonym",
                                                Map.of("synonyms", "Alex, alex, Alexander, alexander => Alex")))));
        assertThat(col).isNotNull();
    }

    @Test
    @Order(5)
    void should_createCollection_withLexicalLetterTokenizer() {
        String name = "c_lexical_letter";
        getDatabase().dropCollection(name);
        Collection<Document> col = getDatabase().createCollection(name,
                new CollectionDefinition().lexical(
                        new LexicalOptions().analyzer(
                                new Analyzer().tokenizer(LETTER.getValue()))));
        assertThat(col).isNotNull();
    }

    // ========== Collection creation — vector + lexical + reranking (Nvidia) ==========

    @Test
    @Order(6)
    void should_createCollection_withVectorLexicalAndRerank() {
        getDatabase().dropCollection(COLLECTION_FIND_RERANK);

        // Nvidia NV-Embed-QA — built-in, no external API key required
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

        CollectionDefinition def = new CollectionDefinition()
                .vector(vectorOptions)
                .lexical(lexicalOptions)
                .rerank(rerankOptions);

        Collection<Document> col = getDatabase().createCollection(COLLECTION_FIND_RERANK, def);
        assertThat(col).isNotNull();
        assertThat(col.getDefinition().getLexical()).isNotNull();
        assertThat(col.getDefinition().getRerank()).isNotNull();
    }

    // ========== Populate with philosopher quotes ==========

    @Test
    @Order(7)
    void should_insertMany_philosopherQuotes() throws IOException {
        Collection<Document> col = getDatabase().getCollection(COLLECTION_FIND_RERANK);
        col.deleteAll();

        List<Document> docs = Files.readAllLines(
                        Paths.get("src/test/resources/philosopher-quotes.csv"))
                .stream()
                .skip(1) // skip header
                .map(line -> {
                    String[] chunks = line.split(",", 3);
                    String author = chunks[0];
                    String quote = chunks.length > 1 ? chunks[1].replace("\"", "") : "";
                    return new Document()
                            .put("author", author)
                            .put("quote", quote)
                            .lexical(quote)
                            .vectorize(quote);
                }).toList();

        // No embedding auth needed — Nvidia NV-Embed-QA is built-in
        CollectionInsertManyResult res = col.insertMany(docs,
                new CollectionInsertManyOptions()
                        .concurrency(3)
                        .chunkSize(10));
        assertThat(res.getInsertedIds()).isNotEmpty();
        log.info("Inserted {} philosopher quotes", res.getInsertedIds().size());
    }

    // ========== findAndRerank — hybrid sort ==========

    @Test
    @Order(8)
    void should_findAndRerank_withHybridSort() {
        if (skipIfNoRerankingKey()) return;

        CollectionFindAndRerankOptions options = baseFindAndRerankOptions()
                .sort(Sort.hybrid(new Hybrid("We struggle all in life")))
                .projection(Projection.include(DataAPIKeywords.VECTORIZE.getKeyword()))
                .hybridLimits(10);

        List<RerankedResult<Document>> results = getDatabase()
                .getCollection(COLLECTION_FIND_RERANK)
                .findAndRerank(null, options)
                .toList();

        assertThat(results).isNotEmpty();
        results.forEach(r -> {
            assertThat(r.getDocument()).isNotNull();
            assertThat(r.getScores()).isNotNull();
            log.info("Score: {} — Doc: {}", r.getScores(), r.getDocument().getString("quote"));
        });
    }

    @Test
    @Order(9)
    void should_findAndRerank_withFilter() {
        if (skipIfNoRerankingKey()) return;

        CollectionFindAndRerankOptions options = baseFindAndRerankOptions()
                .sort(Sort.hybrid(new Hybrid("knowledge and wisdom")))
                .hybridLimits(10);

        List<RerankedResult<Document>> results = getDatabase()
                .getCollection(COLLECTION_FIND_RERANK)
                .findAndRerank(Filters.eq("author", "aristotle"), options)
                .toList();

        assertThat(results).isNotEmpty();
        // All results should be from aristotle
        results.forEach(r ->
                assertThat(r.getDocument().getString("author")).isEqualTo("aristotle"));
    }

    @Test
    @Order(10)
    void should_findAndRerank_withRerankOnAndRerankQuery() {
        if (skipIfNoRerankingKey()) return;

        CollectionFindAndRerankOptions options = baseFindAndRerankOptions()
                .sort(Sort.hybrid(new Hybrid("We struggle all in life")))
                .rerankOn(DataAPIKeywords.VECTORIZE.getKeyword())
                .rerankQuery("We struggle all in life")
                .hybridLimits(20);

        List<RerankedResult<Document>> results = getDatabase()
                .getCollection(COLLECTION_FIND_RERANK)
                .findAndRerank(options)
                .toList();

        assertThat(results).isNotEmpty();
        log.info("findAndRerank with rerankOn returned {} results", results.size());
    }

    @Test
    @Order(11)
    void should_findAndRerank_withHybridTextShortcut() {
        if (skipIfNoRerankingKey()) return;

        // Using Sort.hybrid(String) shortcut — auto-populates both vectorize and lexical
        CollectionFindAndRerankOptions options = baseFindAndRerankOptions()
                .sort(Sort.hybrid("The meaning of life"))
                .hybridLimits(5);

        List<RerankedResult<Document>> results = getDatabase()
                .getCollection(COLLECTION_FIND_RERANK)
                .findAndRerank(options)
                .toList();

        assertThat(results).isNotEmpty();
        assertThat(results.size()).isLessThanOrEqualTo(10);
    }

    @Test
    @Order(12)
    void should_findAndRerank_withSeparateVectorizeAndLexical() {
        if (skipIfNoRerankingKey()) return;

        // Using Sort.hybrid(vectorize, lexical) with different texts
        CollectionFindAndRerankOptions options = baseFindAndRerankOptions()
                .sort(Sort.hybrid("struggle and hardship", "struggle life"))
                .hybridLimits(10);

        List<RerankedResult<Document>> results = getDatabase()
                .getCollection(COLLECTION_FIND_RERANK)
                .findAndRerank(options)
                .toList();

        assertThat(results).isNotEmpty();
    }

    @Test
    @Order(13)
    void should_findAndRerank_withProjection() {
        if (skipIfNoRerankingKey()) return;

        CollectionFindAndRerankOptions options = baseFindAndRerankOptions()
                .sort(Sort.hybrid(new Hybrid("education and learning")))
                .projection(Projection.include("author", "quote"))
                .hybridLimits(5);

        List<RerankedResult<Document>> results = getDatabase()
                .getCollection(COLLECTION_FIND_RERANK)
                .findAndRerank(options)
                .toList();

        assertThat(results).isNotEmpty();
        // Verify projection applied — only author and quote should be present
        results.forEach(r -> {
            assertThat(r.getDocument().getString("author")).isNotNull();
            assertThat(r.getDocument().getString("quote")).isNotNull();
        });
    }

    @Test
    @Order(14)
    void should_findAndRerank_withLimit() {
        if (skipIfNoRerankingKey()) return;

        CollectionFindAndRerankOptions options = baseFindAndRerankOptions()
                .sort(Sort.hybrid(new Hybrid("virtue and ethics")))
                .limit(3)
                .hybridLimits(10);

        List<RerankedResult<Document>> results = getDatabase()
                .getCollection(COLLECTION_FIND_RERANK)
                .findAndRerank(options)
                .toList();

        assertThat(results).isNotEmpty();
        assertThat(results.size()).isLessThanOrEqualTo(3);
    }

    // ========== Lexical search — findOne with match ==========

    @Test
    @Order(15)
    void should_findOne_withLexicalMatch() {
        // Use the rerank collection which has lexical enabled
        Collection<Document> col = getDatabase().getCollection(COLLECTION_FIND_RERANK);

        Optional<Document> doc = col.findOne(Filters.match("Fortune favours the bold"));
        assertThat(doc).isPresent();
        log.info("Lexical match found: {}", doc.get().getString("quote"));
    }

    @Test
    @Order(16)
    void should_find_withLexicalMatchFilter() {
        Collection<Document> col = getDatabase().getCollection(COLLECTION_FIND_RERANK);

        // Lexical search uses Filters.match() in a regular find
        List<Document> results = col.find(
                Filters.match("wisdom"),
                new CollectionFindOptions().limit(5)).toList();
        assertThat(results).isNotEmpty();
        log.info("Lexical match filter returned {} results", results.size());
    }

    // ========== Hybrid object construction ==========

    @Test
    @Order(17)
    void should_findAndRerank_withHybridObjectCombination() {
        if (skipIfNoRerankingKey()) return;

        // Build Hybrid with separate vectorize and lexical
        Hybrid hybrid = new Hybrid()
                .vectorize("The purpose of education is learning")
                .lexical("education learning purpose");

        CollectionFindAndRerankOptions options = baseFindAndRerankOptions()
                .sort(Sort.hybrid(hybrid))
                .hybridLimits(10);

        List<RerankedResult<Document>> results = getDatabase()
                .getCollection(COLLECTION_FIND_RERANK)
                .findAndRerank(options)
                .toList();

        assertThat(results).isNotEmpty();
    }

    // ========== findAndRerank — getSortVector ==========

    @Test
    @Order(18)
    void should_findAndRerank_getSortVectorBeforeConsuming() {
        if (skipIfNoRerankingKey()) return;

        CollectionFindAndRerankCursor<Document, Document> cursor = getDatabase()
                .getCollection(COLLECTION_FIND_RERANK)
                .findAndRerank(baseFindAndRerankOptions()
                        .sort(Sort.hybrid(new Hybrid("We struggle all in life")))
                        .includeSortVector(true)
                        .hybridLimits(10));

        // getSortVector() before consuming should trigger page fetch and return the vector
        Optional<DataAPIVector> sortVector = cursor.getSortVector();
        assertThat(sortVector).isPresent();
        log.info("Sort vector (before consuming): dimension={}", sortVector.get().getEmbeddings().length);

        // Cursor should still be iterable after getSortVector()
        List<RerankedResult<Document>> results = cursor.toList();
        assertThat(results).isNotEmpty();
    }

    @Test
    @Order(19)
    void should_findAndRerank_getSortVectorAfterConsuming() {
        if (skipIfNoRerankingKey()) return;

        CollectionFindAndRerankCursor<Document, Document> cursor = getDatabase()
                .getCollection(COLLECTION_FIND_RERANK)
                .findAndRerank(baseFindAndRerankOptions()
                        .sort(Sort.hybrid(new Hybrid("We struggle all in life")))
                        .includeSortVector(true)
                        .hybridLimits(10));

        // Consume the cursor first
        List<RerankedResult<Document>> results = cursor.toList();
        assertThat(results).isNotEmpty();

        // getSortVector() after consuming should still return the vector from the fetched page
        Optional<DataAPIVector> sortVector = cursor.getSortVector();
        assertThat(sortVector).isPresent();
        log.info("Sort vector (after consuming): dimension={}", sortVector.get().getEmbeddings().length);
    }

    // ========== findAndRerank — cursor builder methods ==========

    @Test
    @Order(20)
    void should_findAndRerank_cursorLimit_notCauseStackOverflow() {
        if (skipIfNoRerankingKey()) return;

        CollectionFindAndRerankCursor<Document, Document> cursor = getDatabase()
                .getCollection(COLLECTION_FIND_RERANK)
                .findAndRerank(baseFindAndRerankOptions()
                        .sort(Sort.hybrid(new Hybrid("virtue and ethics")))
                        .hybridLimits(10));

        // cursor.limit() should set the limit on the options, not recurse
        List<RerankedResult<Document>> results = cursor.limit(2).toList();
        assertThat(results).isNotEmpty();
        assertThat(results.size()).isLessThanOrEqualTo(2);
    }

    @Test
    @Order(21)
    void should_findAndRerank_cursorIncludeSortVector_notCauseStackOverflow() {
        if (skipIfNoRerankingKey()) return;

        CollectionFindAndRerankCursor<Document, Document> cursor = getDatabase()
                .getCollection(COLLECTION_FIND_RERANK)
                .findAndRerank(baseFindAndRerankOptions()
                        .sort(Sort.hybrid(new Hybrid("knowledge and wisdom")))
                        .hybridLimits(10));

        // cursor.includeSortVector() should set the flag, not recurse
        CollectionFindAndRerankCursor<Document, Document> cursorWithVector = cursor.includeSortVector();
        Optional<DataAPIVector> sortVector = cursorWithVector.getSortVector();
        assertThat(sortVector).isPresent();
        log.info("Sort vector via cursor builder: dimension={}", sortVector.get().getEmbeddings().length);
    }

    @Test
    @Order(22)
    void should_findAndRerank_cursorBuilderMethods_notMutateOriginalOptions() {
        if (skipIfNoRerankingKey()) return;

        CollectionFindAndRerankCursor<Document, Document> original = getDatabase()
                .getCollection(COLLECTION_FIND_RERANK)
                .findAndRerank(baseFindAndRerankOptions()
                        .sort(Sort.hybrid(new Hybrid("education and learning")))
                        .limit(5)
                        .hybridLimits(10));

        // Creating a derived cursor with limit(2) should not affect the original
        CollectionFindAndRerankCursor<Document, Document> derived = original.limit(2);
        List<RerankedResult<Document>> derivedResults = derived.toList();
        assertThat(derivedResults).isNotEmpty();
        assertThat(derivedResults.size()).isLessThanOrEqualTo(2);

        // Original should still return up to 5
        List<RerankedResult<Document>> originalResults = original.toList();
        assertThat(originalResults).hasSizeGreaterThan(2);
    }

    // ========== findAndRerank — includeScores false / omitted ==========

    @Test
    @Order(23)
    void should_findAndRerank_withIncludeScoresFalse() {
        if (skipIfNoRerankingKey()) return;

        // Explicitly set includeScores(false) — should NOT throw NPE on status being null
        CollectionFindAndRerankOptions options = new CollectionFindAndRerankOptions()
                .rerankingAuthProvider(getRerankingAuthProvider())
                .sort(Sort.hybrid(new Hybrid("We struggle all in life")))
                .includeScores(false)
                .hybridLimits(10)
                .limit(5);

        List<RerankedResult<Document>> results = getDatabase()
                .getCollection(COLLECTION_FIND_RERANK)
                .findAndRerank(options)
                .toList();

        assertThat(results).isNotEmpty();
        // Scores should be null when includeScores is false
        results.forEach(r -> {
            assertThat(r.getDocument()).isNotNull();
            assertThat(r.getScores()).isNull();
        });
    }

    @Test
    @Order(24)
    void should_findAndRerank_withIncludeScoresOmitted() {
        if (skipIfNoRerankingKey()) return;

        // Do not set includeScores at all (defaults to false) — should NOT throw NPE
        CollectionFindAndRerankOptions options = new CollectionFindAndRerankOptions()
                .rerankingAuthProvider(getRerankingAuthProvider())
                .sort(Sort.hybrid(new Hybrid("We struggle all in life")))
                .includeScores(null)
                .hybridLimits(10)
                .limit(5);

        List<RerankedResult<Document>> results = getDatabase()
                .getCollection(COLLECTION_FIND_RERANK)
                .findAndRerank(options)
                .toList();

        assertThat(results).isNotEmpty();
        results.forEach(r -> assertThat(r.getDocument()).isNotNull());
    }

    // ========== replaceOne with vectorize sort ==========

    @Test
    @Order(25)
    void should_replaceOne_withVectorizeSort() {
        if (skipIfNoRerankingKey()) return;

        Collection<Document> col = getDatabase().getCollection(COLLECTION_FIND_RERANK);
        col.deleteAll();

        // Insert documents with vectorize content
        col.insertMany(List.of(
                new Document().id("rp1").put("quote", "The stars shine bright in the night sky").vectorize("The stars shine bright in the night sky"),
                new Document().id("rp2").put("quote", "Mathematics is the queen of sciences").vectorize("Mathematics is the queen of sciences"),
                new Document().id("rp3").put("quote", "Music soothes the soul").vectorize("Music soothes the soul")),
                new CollectionInsertManyOptions().chunkSize(3));

        // Replace the document most similar to a math query using vectorize sort
        CollectionUpdateResult res = col.replaceOne(
                null,
                new Document().id("rp2").put("quote", "Math is beautiful").vectorize("Math is beautiful"),
                new CollectionReplaceOneOptions().sort(Sort.vectorize("Mathematics and numbers")));
        assertThat(res.getMatchedCount()).isEqualTo(1);
        assertThat(res.getModifiedCount()).isEqualTo(1);

        // Verify the math document was replaced
        Optional<Document> doc = col.findById("rp2");
        assertThat(doc).isPresent();
        assertThat(doc.get().getString("quote")).isEqualTo("Math is beautiful");
    }
}
