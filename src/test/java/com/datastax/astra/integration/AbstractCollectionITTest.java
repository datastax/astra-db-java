package com.datastax.astra.integration;

import com.datastax.astra.TestConstants;
import com.datastax.astra.client.Collection;
import com.datastax.astra.client.DataAPIOptions;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.DatabaseAdmin;
import com.datastax.astra.client.exception.TooManyDocumentsToCountException;
import com.datastax.astra.client.model.Command;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.api.ApiResponse;
import com.datastax.astra.client.model.collections.CollectionOptions;
import com.datastax.astra.client.model.delete.DeleteOneOptions;
import com.datastax.astra.client.model.find.FindOneAndReplaceOptions;
import com.datastax.astra.client.model.find.FindOneOptions;
import com.datastax.astra.client.model.find.FindOptions;
import com.datastax.astra.client.model.find.SimilarityMetric;
import com.datastax.astra.client.model.find.SortOrder;
import com.datastax.astra.client.model.insert.InsertManyOptions;
import com.datastax.astra.client.model.insert.InsertOneResult;
import com.datastax.astra.client.model.iterable.DistinctIterable;
import com.datastax.astra.client.model.iterable.FindIterable;
import com.datastax.astra.client.model.update.UpdateResult;
import com.datastax.astra.client.observer.LoggingCommandObserver;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.datastax.astra.client.model.filter.Filters.eq;
import static com.datastax.astra.client.model.filter.Filters.gt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractCollectionITTest  implements TestConstants {

    /** Tested Store. */
    static DatabaseAdmin databaseAdmin;

    /** Tested Namespace. */
    static Database database;

    /** Tested collection1. */
    protected static Collection<Document> collectionSimple;

    /** Tested collection2. */
    protected static Collection<Product> collectionVector;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Product {
        @JsonProperty("_id")
        private Object objectId;
        @JsonProperty("product_name")
        private String name;
        @JsonProperty("product_price")
        private Double price;
    }

    /**
     * Generating sample document to insert.
     *
     * @param size
     *      size of the list
     * @return
     *      number of documents
     */
    protected List<Document> generateDocList(int size) {
        return  IntStream
                .range(0, size)
                .mapToObj(idx ->Document.create(idx).append("indice", idx))
                .collect(Collectors.toList());
    }

    protected Collection<Document> getCollectionSimple() {
        if (collectionSimple == null) {
            collectionSimple = getDatabase().createCollection(COLLECTION_SIMPLE);
            collectionSimple.registerListener("logger", new LoggingCommandObserver(Collection.class));
        }
        return collectionSimple;
    }

    protected Collection<Product> getCollectionVector() {
        if (collectionVector == null) {
            collectionVector = getDatabase().createCollection(COLLECTION_VECTOR,
                    CollectionOptions
                            .builder()
                            .withVectorDimension(14)
                            .withVectorSimilarityMetric(SimilarityMetric.cosine)
                            .build(), Product.class);
            collectionVector.registerListener("logger", new LoggingCommandObserver(Collection.class));
        }

        return collectionVector;
    }

    protected synchronized Database getDatabase() {
        if (database == null) {
            AbstractCollectionITTest.database = initDatabase();
        }
        return database;
    }

    protected abstract Database initDatabase();

    @Test
    @Order(1)
    public void shouldPopulateGeneralInformation() {
        assertThat(getCollectionSimple().getOptions()).isNotNull();
        assertThat(getCollectionSimple().getName()).isNotNull();
        assertThat(getCollectionSimple().getDocumentClass()).isNotExactlyInstanceOf(Document.class);
        assertThat(getCollectionVector().getOptions()).isNotNull();
        assertThat(getCollectionVector().getName()).isNotNull();
        assertThat(getCollectionVector().getDocumentClass()).isNotExactlyInstanceOf(Document.class);
    }

    @Test
    @Order(2)
    public void testInsertOne() {
        // Given
        InsertOneResult res1 = getCollectionSimple()
                .insertOne(new Document().append("hello", "world"));
        // Then
        assertThat(res1).isNotNull();
        assertThat(res1.getInsertedId()).isNotNull();

        Product product = new Product(null, "cool", 9.99);
        InsertOneResult res2 = getCollectionVector().insertOne(product);
        assertThat(res2).isNotNull();
        assertThat(res2.getInsertedId()).isNotNull();
    }

    @Test
    public void testFindOne() {
        getCollectionVector().deleteAll();
        getCollectionVector().insertOne(new Product(1, "cool", 9.99));

        // Find One with no options
        Optional<Product> doc = getCollectionVector().findOne(eq(1));

        // Find One with a filter and projection
        Optional<Product> doc2 = getCollectionVector().findOne(eq(1), new FindOneOptions()
                .projection(Map.of("product_name",1)));

        // Find One with a projection only
        Optional<Product> doc3 = getCollectionVector().findOne(null, new FindOneOptions()
                .projection(Map.of("product_name",1)));
    }

    @Test
    public void testRunCommand() {
        getCollectionSimple().deleteAll();

        ApiResponse res = getCollectionSimple().runCommand(Command
                .create("insertOne")
                .withDocument(new Document().id(1).append("product_name", "hello")));
        assertThat(res).isNotNull();
        assertThat(res.getStatus().getList("insertedIds", Object.class).get(0)).isEqualTo(1);

        Command findOne = Command.create("findOne").withFilter(eq(1));
        res = getCollectionSimple().runCommand(findOne);
        assertThat(res).isNotNull();
        assertThat(res.getData()).isNotNull();
        assertThat(res.getData().getDocument()).isNotNull();
        Document doc = res.getData().getDocument();
        assertThat(doc.getString("product_name")).isEqualTo("hello");

        Document doc2 = getCollectionSimple().runCommand(findOne, Document.class);
        assertThat(doc2).isNotNull();

        Product p1 = getCollectionSimple().runCommand(findOne, Product.class);
        assertThat(p1).isNotNull();
        assertThat(p1.getName()).isEqualTo("hello");
    }

    @Test
    public void testCountDocument() throws TooManyDocumentsToCountException {

        assertThatThrownBy(() -> getCollectionSimple().countDocuments(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UpperBound");
        assertThatThrownBy(() -> getCollectionSimple().countDocuments(2000))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UpperBound");

        getCollectionSimple().deleteAll();
        // Insert 10 documents
        for(int i=0;i<10;i++) {
            getCollectionSimple().insertOne(new Document().id(i).append("indice", i));
        }
        assertThat(getCollectionSimple().countDocuments(1000)).isEqualTo(10);
        assertThatThrownBy(() -> getCollectionSimple().countDocuments(9))
                .isInstanceOf(TooManyDocumentsToCountException.class)
                .hasMessageContaining("upper bound ");

        // Add a filter
        assertThat(getCollectionSimple()
                .countDocuments(gt("indice", 3), DataAPIOptions.getMaxDocumentCount()))
                .isEqualTo(6);

        // Filter + limit
        for(int i=11;i<1005;i++) {
            getCollectionSimple().insertOne(new Document().id(i).append("indice", i));
        }

        // More than 1000 items
        assertThatThrownBy(() -> getCollectionSimple().countDocuments(DataAPIOptions.getMaxDocumentCount()))
                .isInstanceOf(TooManyDocumentsToCountException.class)
                .hasMessageContaining("server");
    }

    @Test
    public void testFindAll() {
        // Populate the Collection
        getCollectionSimple().deleteAll();
        for(int i=0;i<25;i++) getCollectionSimple().insertOne(Document.create(i).append("indice", i));

        FindIterable<Document> findIterable  = getCollectionSimple().find();
        for (Document document : findIterable) assertThat(document).isNotNull();

        List<Document> documents = getCollectionSimple().find().all();
        assertThat(documents.size()).isEqualTo(25);
    }

    @Test
    public void testFindSkipLimit() {
        // Populate the Collection
        getCollectionSimple().deleteAll();
        for(int i=0;i<25;i++) getCollectionSimple().insertOne(Document.create(i).append("indice", i));

        // Sort = no paging
        FindOptions options = new FindOptions().sortingBy("indice", SortOrder.ASCENDING).skip(11).limit(2);
        List<Document> documents = getCollectionSimple().find(options).all();
        assertThat(documents.size()).isEqualTo(2);
        assertThat(documents.get(0).getInteger("indice")).isEqualTo(11);
        assertThat(documents.get(1).getInteger("indice")).isEqualTo(12);
    }

    @Test
    public void testInsertManySinglePage() throws TooManyDocumentsToCountException {
        getCollectionSimple().deleteAll();
        List<Document> docList = generateDocList(10);
        getCollectionSimple().insertMany(docList);
        assertThat(getCollectionSimple().countDocuments(100)).isEqualTo(10);
    }

    @Test
    public void testInsertManyWithPaging() throws TooManyDocumentsToCountException {
        getCollectionSimple().deleteAll();
        List<Document> docList = generateDocList(25);
        getCollectionSimple().insertMany(docList);
        assertThat(getCollectionSimple().countDocuments(100)).isEqualTo(25);
    }

    @Test
    public void testInsertManyWithPagingDistributed() throws TooManyDocumentsToCountException {
        getCollectionSimple().deleteAll();
        List<Document> docList = generateDocList(55);
        getCollectionSimple().insertMany(docList, InsertManyOptions.builder().concurrency(5).build());
        assertThat(getCollectionSimple().countDocuments(100)).isEqualTo(55);
    }

    @Test
    public void testDistinct() {
        // Given a collection with 25 items
        getCollectionSimple().deleteAll();
        for(int i=0;i<25;i++) getCollectionSimple().insertOne(Document.create(i).append("indice", i%7));
        // Look for
        DistinctIterable<Document, Integer> dis = getCollectionSimple().distinct("indice", Integer.class);
        List<Integer> distinctList = dis.all();
        assertThat(distinctList).hasSize(7);
    }

    @Test
    public void testDistinctIter() {
        // Given a collection with 25 items
        int distinct = 22;
        getCollectionSimple().deleteAll();
        getCollectionSimple().insertMany(IntStream
                .range(0,25)
                .mapToObj(i -> Document.create(i).append("indice", i%distinct))
                .collect(Collectors.toList()));
        // Iterate over the collection distinct
        List<Integer> values = new ArrayList<>();
        for (Integer integer : getCollectionSimple().distinct("indice", Integer.class)) {
            values.add(integer);
        }
        assertThat(values).hasSize(distinct);

        int smallDistinct = 7;
        getCollectionSimple().deleteAll();
        getCollectionSimple().insertMany(IntStream
                .range(0,25)
                .mapToObj(i -> Document.create(i).append("indice", i%smallDistinct))
                .collect(Collectors.toList()));
        // Iterate over the collection distinct
        values = new ArrayList<>();
        try {
            for (Integer integer : getCollectionSimple().distinct("indice", Integer.class)) {
                values.add(integer);
            }
        } catch(NoSuchElementException e) {
        }
        assertThat(values).hasSize(smallDistinct);
    }

    @Test
    public void testDeleteOne() {
        // Insert 3 items
        getCollectionSimple().deleteAll();
        getCollectionSimple().insertMany(IntStream
                .range(0, 3)
                .mapToObj(i -> Document.create(i).append("indice", i).append("test", "test"))
                .collect(Collectors.toList()));

        // Delete exactly one
        getCollectionSimple().deleteOne(eq("indice", 1));
        Map<Integer, Document> results = getCollectionSimple()
                .find().all()
                .stream().collect(Collectors
                        .toMap(doc-> doc.getId(Integer.class), Function.identity()));
        assertThat(results).hasSize(2);
        assertThat(results).containsKey(0);
        assertThat(results).containsKey(2);
        assertThat(results).doesNotContainKey(1);

        // Delete one with a filter as 3 matches
        // Insert 3 items
        getCollectionSimple().deleteAll();
        getCollectionSimple().insertMany(IntStream
                .range(0, 3)
                .mapToObj(i -> Document.create(i).append("indice", i).append("test", "test"))
                .collect(Collectors.toList()));
        getCollectionSimple().deleteOne(eq("test", "test"),
                new DeleteOneOptions().sortingBy("indice", SortOrder.DESCENDING));
        results = getCollectionSimple()
                .find().all()
                .stream().collect(Collectors
                        .toMap(doc-> doc.getId(Integer.class), Function.identity()));
        assertThat(results).hasSize(2);
        assertThat(results).containsKey(0);
        assertThat(results).containsKey(1);
        assertThat(results).doesNotContainKey(2);

    }

    // FindOneAndReplace
    @Test
    public void testFindOneAndReplace() {
        getCollectionSimple().deleteAll();
        getCollectionSimple().insertOne(new Document().id(1).append("hello", "world"));
        getCollectionSimple().insertOne(new Document().id(2).append("bonjour", "monde"));

        // Matched 1, modified 1, document is present
        Optional<Document> opt1 = getCollectionSimple()
                .findOneAndReplace(eq(1), new Document().id(1).append("hello", "world2"));
        assertThat(opt1.isPresent()).isTrue();
        assertThat(opt1.get().get("hello")).isEqualTo("world2");

        // Matched 1, modified 0, document is present
        Optional<Document> opt2 = getCollectionSimple()
                .findOneAndReplace(eq(1), new Document().id(1).append("hello", "world2"));
        assertThat(opt2.isPresent()).isTrue();
        assertThat(opt2.get().get("hello")).isEqualTo("world2");

        // Matched 0, modified 0, no document returned
        Optional<Document> opt3 = getCollectionSimple()
                .findOneAndReplace(eq(3), new Document().id(3).append("hello", "world2"),
                        new FindOneAndReplaceOptions().upsert(false));
        assertThat(opt3.isPresent()).isFalse();
    }

    @Test
    public void testReplaceOne() {
        getCollectionSimple().deleteAll();
        getCollectionSimple().insertOne(new Document().id(1).append("hello", "world"));
        getCollectionSimple().insertOne(new Document().id(2).append("bonjour", "monde"));

        UpdateResult u1 = getCollectionSimple()
                .replaceOne(eq(1), new Document().id(1).append("hello", "world2"));
        assertThat(u1.getMatchedCount()).isEqualTo(1);
        assertThat(u1.getModifiedCount()).isEqualTo(1);
        assertThat(u1.getUpsertedId()).isEqualTo(1);

        UpdateResult u2 = getCollectionSimple()
                .replaceOne(eq(3), new Document().id(3).append("hello", "world2"));
        assertThat(u2.getMatchedCount()).isEqualTo(0);
        assertThat(u2.getModifiedCount()).isEqualTo(0);
        assertThat(u2.getUpsertedId()).isNull();
    }

    @Test
    public void testUpdateOne() {

    }

}
