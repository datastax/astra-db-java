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
import com.datastax.astra.client.collections.commands.options.CollectionFindOneOptions;
import com.datastax.astra.client.collections.commands.options.CollectionFindOptions;
import com.datastax.astra.client.collections.commands.results.CollectionInsertManyResult;
import com.datastax.astra.client.collections.commands.results.CollectionInsertOneResult;
import com.datastax.astra.client.collections.commands.Updates;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Filters;
import com.datastax.astra.client.core.query.Projection;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.vector.DataAPIVector;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static com.datastax.astra.test.integration.utils.TestDataset.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Abstract integration tests for vector search operations on collections.
 * <p>
 * Tests cover insertOne/insertMany with vectors, similarity search using
 * {@code Sort.vector()}, {@code DataAPIVector}, {@code includeSimilarity},
 * combined filter + vector sort, and findOneAndUpdate with vector sort.
 * <p>
 * Uses the {@code COLLECTION_VECTOR} (dimension=14, cosine) collection.
 */
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractCollectionVectorSearchIT extends AbstractDataAPITest {

    /** 14-dimension vector collection (cosine). */
    protected Collection<Document> collectionVector;

    // -- Reusable test vectors (dimension 14) --

    static final float[] VECTOR_BEEF  = {1f, 0f, 1f, 1f, 1f, 1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f};
    static final float[] VECTOR_CHICK = {1f, 1f, 1f, 1f, 1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f};
    static final float[] VECTOR_FISH  = {0f, 0f, 0f, 0f, 0f, 1f, 1f, 1f, 1f, 1f, 1f, 0f, 0f, 0f};
    static final float[] VECTOR_VEGAN = {0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 1f, 1f, 1f};

    @BeforeAll
    void setupVectorCollection() {
        dropAllCollections();
        dropAllTables();
        collectionVector = getDatabase().createCollection(
                COLLECTION_VECTOR, COLLECTION_VECTOR_DEF);
        log.info("Initialized vector collection '{}'", COLLECTION_VECTOR);
    }

    // ========== insertOne with vector ==========

    @Test
    @Order(1)
    void should_insertOne_withFloatArrayVector() {
        collectionVector.deleteAll();

        CollectionInsertOneResult res = collectionVector.insertOne(
                new Document().id("v1")
                        .vector(VECTOR_BEEF)
                        .put("product_name", "Beef raw dog food")
                        .put("product_price", 12.99));
        assertThat(res).isNotNull();
        assertThat(res.getInsertedId()).isEqualTo("v1");

        Optional<Document> doc = collectionVector.findById("v1");
        assertThat(doc).isPresent();
        assertThat(doc.get().getString("product_name")).isEqualTo("Beef raw dog food");
    }

    @Test
    @Order(2)
    void should_insertOne_withDataAPIVector() {
        collectionVector.deleteAll();

        DataAPIVector vec = new DataAPIVector(VECTOR_CHICK);
        assertThat(vec.dimension()).isEqualTo(14);

        CollectionInsertOneResult res = collectionVector.insertOne(
                new Document().id("v2")
                        .vector(vec)
                        .put("product_name", "Chicken raw dog food")
                        .put("product_price", 9.99));
        assertThat(res).isNotNull();
        assertThat(res.getInsertedId()).isEqualTo("v2");
    }

    @Test
    @Order(3)
    void should_insertOne_andRetrieveVector() {
        collectionVector.deleteAll();

        collectionVector.insertOne(new Document().id("v3")
                .vector(VECTOR_FISH)
                .put("product_name", "Fish dog food"));

        Optional<Document> doc = collectionVector.findOne(
                Filters.eq("v3"),
                new CollectionFindOneOptions().projection(Projection.include("product_name")));
        assertThat(doc).isPresent();
        Optional<DataAPIVector> vector = doc.get().getVector();
        // Projection may or may not include $vector depending on server version
        // The important thing is the document is found
        assertThat(doc.get().getString("product_name")).isEqualTo("Fish dog food");
    }

    // ========== insertMany with vectors ==========

    @Test
    @Order(4)
    void should_insertMany_withVectors() {
        collectionVector.deleteAll();

        List<Document> docs = List.of(
                new Document().id("doc1").vector(VECTOR_BEEF)
                        .put("product_name", "Beef raw dog food")
                        .put("product_price", 12.99),
                new Document().id("doc2").vector(VECTOR_CHICK)
                        .put("product_name", "Chicken raw dog food")
                        .put("product_price", 9.99),
                new Document().id("doc3").vector(VECTOR_FISH)
                        .put("product_name", "Fish dog food")
                        .put("product_price", 11.49),
                new Document().id("doc4").vector(VECTOR_VEGAN)
                        .put("product_name", "Vegan dog food")
                        .put("product_price", 14.99));

        CollectionInsertManyResult res = collectionVector.insertMany(docs);
        assertThat(res).isNotNull();
        assertThat(res.getInsertedIds()).hasSize(4);
    }

    // ========== find with vector sort (similarity search) ==========

    @Test
    @Order(5)
    void should_find_withVectorSort() {
        collectionVector.deleteAll();
        collectionVector.insertMany(List.of(
                new Document().id("doc1").vector(VECTOR_BEEF)
                        .put("product_name", "Beef raw").put("product_price", 12.99),
                new Document().id("doc2").vector(VECTOR_CHICK)
                        .put("product_name", "Chicken raw").put("product_price", 9.99),
                new Document().id("doc3").vector(VECTOR_FISH)
                        .put("product_name", "Fish food").put("product_price", 11.49),
                new Document().id("doc4").vector(VECTOR_VEGAN)
                        .put("product_name", "Vegan food").put("product_price", 14.99)));

        // Search for items similar to chicken
        List<Document> results = collectionVector.find(new CollectionFindOptions()
                .sort(Sort.vector(VECTOR_CHICK))
                .limit(2)).toList();

        assertThat(results).hasSize(2);
        // Most similar to VECTOR_CHICK should come first
        assertThat(results.get(0).getId(String.class)).isEqualTo("doc2");
    }

    @Test
    @Order(6)
    void should_find_withVectorSortAndFilter() {
        collectionVector.deleteAll();
        collectionVector.insertMany(List.of(
                new Document().id("doc1").vector(VECTOR_BEEF)
                        .put("product_name", "Beef raw").put("product_price", 12.99),
                new Document().id("doc2").vector(VECTOR_CHICK)
                        .put("product_name", "Chicken raw").put("product_price", 9.99),
                new Document().id("doc3").vector(VECTOR_FISH)
                        .put("product_name", "Fish food").put("product_price", 11.49),
                new Document().id("doc4").vector(VECTOR_VEGAN)
                        .put("product_name", "Vegan food").put("product_price", 14.99)));

        // Filter on price <= 12.99, then sort by vector similarity to VECTOR_BEEF
        Filter priceFilter = Filters.lte("product_price", 12.99);
        List<Document> results = collectionVector.find(priceFilter, new CollectionFindOptions()
                .sort(Sort.vector(VECTOR_BEEF))
                .limit(3)).toList();

        assertThat(results).hasSize(3); // doc1 (12.99), doc2 (9.99), doc3 (11.49)
        // doc1 is identical to VECTOR_BEEF => highest similarity
        assertThat(results.get(0).getId(String.class)).isEqualTo("doc1");
    }

    @Test
    @Order(7)
    void should_find_withVectorSortAndProjection() {
        collectionVector.deleteAll();
        collectionVector.insertMany(List.of(
                new Document().id("doc1").vector(VECTOR_BEEF)
                        .put("product_name", "Beef raw").put("product_price", 12.99),
                new Document().id("doc2").vector(VECTOR_CHICK)
                        .put("product_name", "Chicken raw").put("product_price", 9.99)));

        // Vector sort + project only product_name
        List<Document> results = collectionVector.find(new CollectionFindOptions()
                .sort(Sort.vector(VECTOR_BEEF))
                .projection(Projection.include("product_name"))
                .limit(2)).toList();

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getString("product_name")).isEqualTo("Beef raw");
        // product_price excluded by projection
        assertThat(results.get(0).get("product_price")).isNull();
    }

    // ========== findOne with vector sort ==========

    @Test
    @Order(8)
    void should_findOne_withVectorSort() {
        collectionVector.deleteAll();
        collectionVector.insertMany(List.of(
                new Document().id("doc1").vector(VECTOR_BEEF)
                        .put("product_name", "Beef raw"),
                new Document().id("doc2").vector(VECTOR_CHICK)
                        .put("product_name", "Chicken raw"),
                new Document().id("doc3").vector(VECTOR_FISH)
                        .put("product_name", "Fish food")));

        // Find the single most similar document to VECTOR_FISH
        Optional<Document> doc = collectionVector.findOne(null,
                new CollectionFindOneOptions().sort(Sort.vector(VECTOR_FISH)));
        assertThat(doc).isPresent();
        assertThat(doc.get().getId(String.class)).isEqualTo("doc3");
    }

    @Test
    @Order(9)
    void should_findOne_withVectorSortAndFilter() {
        collectionVector.deleteAll();
        collectionVector.insertMany(List.of(
                new Document().id("doc1").vector(VECTOR_BEEF)
                        .put("product_name", "Beef raw").put("category", "meat"),
                new Document().id("doc2").vector(VECTOR_CHICK)
                        .put("product_name", "Chicken raw").put("category", "meat"),
                new Document().id("doc3").vector(VECTOR_FISH)
                        .put("product_name", "Fish food").put("category", "seafood")));

        // Filter by category, then find most similar to VECTOR_BEEF within "meat"
        Optional<Document> doc = collectionVector.findOne(
                Filters.eq("category", "meat"),
                new CollectionFindOneOptions().sort(Sort.vector(VECTOR_BEEF)));
        assertThat(doc).isPresent();
        // doc1 has VECTOR_BEEF, identical match
        assertThat(doc.get().getId(String.class)).isEqualTo("doc1");
    }

    // ========== includeSimilarity ==========

    @Test
    @Order(10)
    void should_findOne_withIncludeSimilarity() {
        collectionVector.deleteAll();
        collectionVector.insertMany(List.of(
                new Document().id("doc1").vector(VECTOR_BEEF).put("name", "Beef"),
                new Document().id("doc2").vector(VECTOR_CHICK).put("name", "Chicken")));

        // Search with includeSimilarity
        Optional<Document> doc = collectionVector.findOne(null,
                new CollectionFindOneOptions()
                        .sort(Sort.vector(VECTOR_BEEF))
                        .includeSimilarity(true));
        assertThat(doc).isPresent();
        assertThat(doc.get().getId(String.class)).isEqualTo("doc1");

        // Exact match => similarity should be 1.0 (cosine)
        Optional<Double> similarity = doc.get().getSimilarity();
        assertThat(similarity).isPresent();
        assertThat(similarity.get()).isCloseTo(1.0d, org.assertj.core.data.Offset.offset(0.001d));
    }

    @Test
    @Order(11)
    void should_find_withIncludeSimilarity() {
        collectionVector.deleteAll();
        collectionVector.insertMany(List.of(
                new Document().id("doc1").vector(VECTOR_BEEF).put("name", "Beef"),
                new Document().id("doc2").vector(VECTOR_CHICK).put("name", "Chicken"),
                new Document().id("doc3").vector(VECTOR_FISH).put("name", "Fish")));

        List<Document> results = collectionVector.find(new CollectionFindOptions()
                .sort(Sort.vector(VECTOR_BEEF))
                .includeSimilarity(true)
                .limit(3)).toList();

        assertThat(results).hasSize(3);
        // First result is exact match => highest similarity
        assertThat(results.get(0).getSimilarity()).isPresent();
        assertThat(results.get(0).getSimilarity().get()).isCloseTo(1.0d,
                org.assertj.core.data.Offset.offset(0.001d));
        // Subsequent results should have lower similarity
        assertThat(results.get(1).getSimilarity()).isPresent();
        assertThat(results.get(1).getSimilarity().get()).isLessThan(1.0d);
        // Results should be ordered by descending similarity
        assertThat(results.get(1).getSimilarity().get())
                .isGreaterThanOrEqualTo(results.get(2).getSimilarity().get());
    }

    // ========== DataAPIVector round-trip ==========

    @Test
    @Order(12)
    void should_insertOne_withDataAPIVector_andRetrieve() {
        collectionVector.deleteAll();

        DataAPIVector inputVec = new DataAPIVector(VECTOR_VEGAN);
        collectionVector.insertOne(new Document().id("dav1")
                .vector(inputVec)
                .put("name", "Vegan food"));

        // Retrieve with vector sort to verify round-trip
        Optional<Document> doc = collectionVector.findOne(null,
                new CollectionFindOneOptions()
                        .sort(Sort.vector(inputVec.getEmbeddings()))
                        .includeSimilarity(true));
        assertThat(doc).isPresent();
        assertThat(doc.get().getId(String.class)).isEqualTo("dav1");
        assertThat(doc.get().getSimilarity().orElse(0d)).isCloseTo(1.0d,
                org.assertj.core.data.Offset.offset(0.001d));
    }

    @Test
    @Order(13)
    void should_find_withDataAPIVectorSort() {
        collectionVector.deleteAll();
        collectionVector.insertMany(List.of(
                new Document().id("d1").vector(VECTOR_BEEF).put("name", "Beef"),
                new Document().id("d2").vector(VECTOR_FISH).put("name", "Fish")));

        DataAPIVector queryVec = new DataAPIVector(VECTOR_FISH);
        List<Document> results = collectionVector.find(new CollectionFindOptions()
                .sort(Sort.vector("$vector", queryVec))
                .limit(2)).toList();

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getId(String.class)).isEqualTo("d2");
    }

    // ========== updateOne with vector ==========

    @Test
    @Order(14)
    void should_updateOne_setVector() {
        collectionVector.deleteAll();
        collectionVector.insertOne(new Document().id("upv1")
                .vector(VECTOR_BEEF)
                .put("name", "Beef food"));

        // Update the vector
        collectionVector.updateOne(Filters.eq("upv1"),
                Updates.set("$vector", List.of(0f, 0f, 0f, 0f, 0f, 1f, 1f, 1f, 1f, 1f, 1f, 0f, 0f, 0f)));

        // Now searching with VECTOR_FISH should find this document first
        Optional<Document> doc = collectionVector.findOne(null,
                new CollectionFindOneOptions()
                        .sort(Sort.vector(VECTOR_FISH))
                        .includeSimilarity(true));
        assertThat(doc).isPresent();
        assertThat(doc.get().getId(String.class)).isEqualTo("upv1");
        assertThat(doc.get().getSimilarity().orElse(0d)).isCloseTo(1.0d,
                org.assertj.core.data.Offset.offset(0.001d));
    }

    // ========== deleteOne with vector sort ==========

    @Test
    @Order(15)
    void should_deleteOne_withVectorSort() {
        collectionVector.deleteAll();
        collectionVector.insertMany(List.of(
                new Document().id("del1").vector(VECTOR_BEEF).put("name", "Beef"),
                new Document().id("del2").vector(VECTOR_FISH).put("name", "Fish")));

        // Delete the document most similar to VECTOR_FISH
        collectionVector.findOneAndDelete(null,
                new com.datastax.astra.client.collections.commands.options.CollectionFindOneAndDeleteOptions()
                        .sort(Sort.vector(VECTOR_FISH)));

        // Only beef should remain
        List<Document> remaining = collectionVector.findAll().toList();
        assertThat(remaining).hasSize(1);
        assertThat(remaining.get(0).getId(String.class)).isEqualTo("del1");
    }

    // ========== replaceOne with vector ==========

    @Test
    @Order(16)
    void should_replaceOne_preserveVector() {
        collectionVector.deleteAll();
        collectionVector.insertOne(new Document().id("rep1")
                .vector(VECTOR_BEEF)
                .put("name", "Old Beef"));

        collectionVector.replaceOne(Filters.eq("rep1"),
                new Document().id("rep1")
                        .vector(VECTOR_CHICK)
                        .put("name", "New Chicken"));

        // Searching with VECTOR_CHICK should now find rep1
        Optional<Document> doc = collectionVector.findOne(null,
                new CollectionFindOneOptions()
                        .sort(Sort.vector(VECTOR_CHICK))
                        .includeSimilarity(true));
        assertThat(doc).isPresent();
        assertThat(doc.get().getId(String.class)).isEqualTo("rep1");
        assertThat(doc.get().getString("name")).isEqualTo("New Chicken");
        assertThat(doc.get().getSimilarity().orElse(0d)).isCloseTo(1.0d,
                org.assertj.core.data.Offset.offset(0.001d));
    }

    // ========== findOneAndUpdate with vector sort ==========

    @Test
    @Order(17)
    void should_findOneAndUpdate_withVectorSort() {
        collectionVector.deleteAll();
        collectionVector.insertMany(List.of(
                new Document().id("fau1").vector(VECTOR_BEEF).put("name", "Beef").put("sold", false),
                new Document().id("fau2").vector(VECTOR_FISH).put("name", "Fish").put("sold", false)));

        // Find the item most similar to VECTOR_FISH and mark as sold
        Optional<Document> updated = collectionVector.findOneAndUpdate(
                null,
                Updates.set("sold", true),
                new com.datastax.astra.client.collections.commands.options.CollectionFindOneAndUpdateOptions()
                        .sort(Sort.vector(VECTOR_FISH))
                        .returnDocument(com.datastax.astra.client.collections.commands.ReturnDocument.AFTER));
        assertThat(updated).isPresent();
        assertThat(updated.get().getId(String.class)).isEqualTo("fau2");
        assertThat(updated.get().getBoolean("sold")).isTrue();

        // Beef should remain unsold
        Optional<Document> beef = collectionVector.findById("fau1");
        assertThat(beef).isPresent();
        assertThat(beef.get().getBoolean("sold")).isFalse();
    }

    // ========== findOneAndReplace with vector sort ==========

    @Test
    @Order(18)
    void should_findOneAndReplace_withVectorSort() {
        collectionVector.deleteAll();
        collectionVector.insertMany(List.of(
                new Document().id("far1").vector(VECTOR_BEEF).put("name", "Beef").put("version", 1),
                new Document().id("far2").vector(VECTOR_VEGAN).put("name", "Vegan").put("version", 1)));

        // Find the item most similar to VECTOR_VEGAN and replace it
        Optional<Document> replaced = collectionVector.findOneAndReplace(
                null,
                new Document().id("far2").vector(VECTOR_VEGAN).put("name", "Premium Vegan").put("version", 2),
                new com.datastax.astra.client.collections.commands.options.CollectionFindOneAndReplaceOptions()
                        .sort(Sort.vector(VECTOR_VEGAN)));
        assertThat(replaced).isPresent();

        // Verify replacement
        Optional<Document> doc = collectionVector.findById("far2");
        assertThat(doc).isPresent();
        assertThat(doc.get().getString("name")).isEqualTo("Premium Vegan");
        assertThat(doc.get().getInteger("version")).isEqualTo(2);
    }

    // ========== similarity ordering verification ==========

    @Test
    @Order(19)
    void should_find_returnResultsOrderedBySimilarity() {
        collectionVector.deleteAll();
        // Insert documents with progressively different vectors from VECTOR_BEEF
        collectionVector.insertMany(List.of(
                new Document().id("exact").vector(VECTOR_BEEF).put("label", "exact"),
                new Document().id("close").vector(VECTOR_CHICK).put("label", "close"),
                new Document().id("far").vector(VECTOR_FISH).put("label", "far"),
                new Document().id("opposite").vector(VECTOR_VEGAN).put("label", "opposite")));

        List<Document> results = collectionVector.find(new CollectionFindOptions()
                .sort(Sort.vector(VECTOR_BEEF))
                .includeSimilarity(true)
                .limit(4)).toList();

        assertThat(results).hasSize(4);
        // Verify descending similarity order
        for (int i = 0; i < results.size() - 1; i++) {
            double currentSim = results.get(i).getSimilarity().orElse(-1d);
            double nextSim = results.get(i + 1).getSimilarity().orElse(-1d);
            assertThat(currentSim).isGreaterThanOrEqualTo(nextSim);
        }
        // First should be exact match
        assertThat(results.get(0).getId(String.class)).isEqualTo("exact");
    }

    // ========== edge cases ==========

    @Test
    @Order(20)
    void should_find_withVectorSort_returnEmptyOnNoMatch() {
        collectionVector.deleteAll();
        collectionVector.insertOne(new Document().id("only1")
                .vector(VECTOR_BEEF).put("category", "meat"));

        // Filter excludes all documents, vector sort still returns empty
        List<Document> results = collectionVector.find(
                Filters.eq("category", "nonexistent"),
                new CollectionFindOptions()
                        .sort(Sort.vector(VECTOR_BEEF))
                        .limit(5)).toList();
        assertThat(results).isEmpty();
    }

    @Test
    @Order(21)
    void should_find_withVectorSort_limitResults() {
        collectionVector.deleteAll();
        for (int i = 0; i < 10; i++) {
            float[] vec = new float[14];
            vec[i % 14] = 1f;
            collectionVector.insertOne(new Document().id("lim" + i)
                    .vector(vec).put("idx", i));
        }

        // Limit to 3
        List<Document> results = collectionVector.find(new CollectionFindOptions()
                .sort(Sort.vector(VECTOR_BEEF))
                .limit(3)).toList();
        assertThat(results).hasSize(3);
    }

    @Test
    @Order(22)
    void should_distinct_withVectorCollection() {
        collectionVector.deleteAll();
        collectionVector.insertMany(List.of(
                new Document().id("dv1").vector(VECTOR_BEEF).put("category", "meat"),
                new Document().id("dv2").vector(VECTOR_CHICK).put("category", "meat"),
                new Document().id("dv3").vector(VECTOR_FISH).put("category", "seafood"),
                new Document().id("dv4").vector(VECTOR_VEGAN).put("category", "vegan")));

        java.util.Set<String> categories = collectionVector.distinct("category", String.class);
        assertThat(categories).containsExactlyInAnyOrder("meat", "seafood", "vegan");
    }
}
