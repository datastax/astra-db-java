package com.datastax.astra.test.integration;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.exception.TooManyDocumentsToCountException;
import com.datastax.astra.client.model.collections.CollectionOptions;
import com.datastax.astra.client.model.command.Command;
import com.datastax.astra.client.model.DeleteOneOptions;
import com.datastax.astra.client.model.DistinctIterable;
import com.datastax.astra.client.model.collections.Document;
import com.datastax.astra.client.model.query.Filter;
import com.datastax.astra.client.model.query.Filters;
import com.datastax.astra.client.model.FindIterable;
import com.datastax.astra.client.model.FindOneAndReplaceOptions;
import com.datastax.astra.client.model.FindOneAndUpdateOptions;
import com.datastax.astra.client.model.FindOneOptions;
import com.datastax.astra.client.model.FindOptions;
import com.datastax.astra.client.model.InsertManyOptions;
import com.datastax.astra.client.model.InsertOneResult;
import com.datastax.astra.client.model.types.ObjectId;
import com.datastax.astra.client.model.query.Projection;
import com.datastax.astra.client.model.query.Projections;
import com.datastax.astra.client.model.SimilarityMetric;
import com.datastax.astra.client.model.Update;
import com.datastax.astra.client.model.UpdateResult;
import com.datastax.astra.internal.api.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.datastax.astra.client.model.query.Filters.eq;
import static com.datastax.astra.client.model.query.Filters.gt;
import static com.datastax.astra.client.model.query.Projections.include;
import static com.datastax.astra.client.model.query.Projections.slice;
import static com.datastax.astra.client.model.query.Sorts.ascending;
import static com.datastax.astra.client.model.query.Sorts.descending;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractCollectionITTest extends AbstractDataAPITest {

    /** Tested collection1. */
    protected static Collection<Document> collectionSimple;

    /** Tested collection2. */
    protected static Collection<ProductString> collectionVector;

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
        }
        return collectionSimple;
    }

    protected Collection<ProductString> getCollectionVector() {
        if (collectionVector == null) {
            collectionVector = getDatabase().createCollection(COLLECTION_VECTOR,
                    CollectionOptions
                            .builder()
                            .vectorDimension(14)
                            .vectorSimilarity(SimilarityMetric.COSINE)
                            .build(), ProductString.class);
        }

        return collectionVector;
    }

    @Test
    @Order(1)
    protected void shouldPopulateGeneralInformation() {
        assertThat(getCollectionSimple().getOptions()).isNotNull();
        assertThat(getCollectionSimple().getName()).isNotNull();
        assertThat(getCollectionSimple().getDocumentClass()).isNotExactlyInstanceOf(Document.class);
        assertThat(getCollectionSimple().getKeyspaceName()).isNotNull();
        assertThat(getCollectionVector().getOptions()).isNotNull();
        assertThat(getCollectionVector().getName()).isNotNull();
        assertThat(getCollectionVector().getDocumentClass()).isNotExactlyInstanceOf(Document.class);
        assertThat(getCollectionVector().getKeyspaceName()).isNotNull();
    }

    @Test
    @Order(2)
    protected void testInsertOne() {
        // Given
        InsertOneResult res1 = getCollectionSimple()
                .insertOne(new Document().append("hello", "world"));
        // Then
        assertThat(res1).isNotNull();
        assertThat(res1.getInsertedId()).isNotNull();

        ProductString product = new ProductString();
        product.setName("cool");
        product.setPrice(9.99);
        InsertOneResult res2 = getCollectionVector().insertOne(product);
        assertThat(res2).isNotNull();
        assertThat(res2.getInsertedId()).isNotNull();
    }

    @Test
    protected void testFindOne() {
        getCollectionVector().deleteAll();
        ProductString product = new ProductString();
        product.setId("1");
        product.setName("cool");
        product.setPrice(9.99);
        getCollectionVector().insertOne(product);

        // Find One with no options
        Optional<ProductString> doc = getCollectionVector().findOne(eq("1"));

        // Find One with a filter and projection

        Optional<ProductString> doc2 = getCollectionVector().findOne(
                eq("1"),
               new FindOneOptions().projection(include("name")));

        // Find One with a projection only
        Optional<ProductString> doc3 = getCollectionVector().findOne(null,
                new FindOneOptions().projection(include("name")));
    }

    @Test
    protected void testRunCommand() {
        getCollectionSimple().deleteAll();

        ApiResponse res = getCollectionSimple().runCommand(Command
                .create("insertOne")
                .withDocument(new Document().id(1).append("name", "hello")));
        assertThat(res).isNotNull();
        assertThat(res.getStatus().getList("insertedIds", Object.class).get(0)).isEqualTo(1);

        Command findOne = Command.create("findOne").withFilter(eq(1));
        res = getCollectionSimple().runCommand(findOne);
        assertThat(res).isNotNull();
        assertThat(res.getData()).isNotNull();
        assertThat(res.getData().getDocument()).isNotNull();
        Document doc = res.getData().getDocument();
        assertThat(doc.getString("name")).isEqualTo("hello");

        Document doc2 = getCollectionSimple().runCommand(findOne, Document.class);
        assertThat(doc2).isNotNull();

        ProductString p1 = getCollectionSimple().runCommand(findOne, ProductString.class);
        assertThat(p1).isNotNull();
        assertThat(p1.getName()).isEqualTo("hello");
    }

    @Test
    protected void testEstimatedCount() throws TooManyDocumentsToCountException {
        getCollectionSimple().deleteAll();
        assertThat(getCollectionSimple().estimatedDocumentCount()).isZero();
        getCollectionSimple().insertMany(generateDocList(21));
        assertThat(getCollectionSimple().countDocuments(1000)).isEqualTo(21);
        getCollectionSimple().estimatedDocumentCount();
    }

    @Test
    protected void testFindWithProjectionSlice() {
        getCollectionSimple().deleteAll();
        getCollectionSimple().insertOne(COMPLETE_DOCUMENT);

        // Should return document projecting only metadata_instant
        //Document doc1 = getCollectionSimple().find(null, FindOptions.Builder
        //        .projection(include("metadata_instant")))
        //        .all().get(0);
        //assertThat(doc1.getInstant("metadata_instant")).isNotNull();

        // Should return document projecting only metadata_instant
        //Document doc2 = getCollectionSimple().find(null, FindOptions.Builder
        //                .projection(exclude("_id")))
        //        .all().get(0);
        //assertThat(doc2.getInstant("metadata_instant")).isNotNull();

        Projection[] ps = Projections.include("metadata_float_array");


        // Should return a slice for an array
        Document doc3 = getCollectionSimple().find(null, new FindOptions()
                        .projection(ps[0], slice("metadata_string_array", 1, 2)))
                        .all().get(0);
        String[] strings = doc3.getArray("metadata_string_array", String.class);
        assertThat(strings).hasSize(2);

    }

    @Test
    protected void testCountDocument() throws TooManyDocumentsToCountException {
        new InsertManyOptions().ordered(false)
                .concurrency(5) // recommended
                .chunkSize(20)  // maximum chunk size is 20
                .timeout(100);  // global timeout

        final Collection<Document> collectionSimple = getCollectionSimple();
        assertThatThrownBy(() -> collectionSimple
                .countDocuments(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UpperBound");
        assertThatThrownBy(() -> collectionSimple
                .countDocuments(2000))
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
                .countDocuments(gt("indice", 3), getCollectionSimple().getDataAPIOptions().getMaxDocumentCount())    )
                .isEqualTo(6);

        // Filter + limit
        for(int i=11;i<1005;i+=20) {
            List<Document> docs = new ArrayList<>();
            for(int j=i;j<i+20;j++) {
                docs.add(new Document().id(j).append("indice", j));
            }
            getCollectionSimple().insertMany(docs);
        }

        // More than 1000 items
        assertThatThrownBy(() -> getCollectionSimple()
                .countDocuments(getCollectionSimple().getDataAPIOptions().getMaxDocumentCount()))
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
        assertThat(documents).hasSize(25);
    }

    @Test
    public void testFindSkipLimit() {
        // Populate the Collection
        getCollectionSimple().deleteAll();
        for(int i=0;i<25;i++) getCollectionSimple().insertOne(Document.create(i).append("indice", i));

        // Sort = no paging
        FindOptions options = new FindOptions().sort(ascending("indice")).skip(11).limit(2);
        List<Document> documents = getCollectionSimple().find(options).all();
        assertThat(documents).hasSize(2);
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
        getCollectionSimple().insertMany(docList, new InsertManyOptions().concurrency(5));
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
    public void shouldTestFindOnEmptyResultList() {
        getCollectionSimple().deleteAll();
        // Find All
        List<Document> okDoc = getCollectionSimple().find().all();
        assertThat(okDoc).isEmpty();

        Filter filter = Filters.eq("userId", UUID.randomUUID());
        FindOptions options = new FindOptions().projection(include("transactionHash"));
        List<Document> docs = getCollectionSimple().find(filter, options).all();
        assertThat(docs).isEmpty();

        //DistinctIterable<Document, Integer> dis = getCollectionSimple().distinct("indice", Integer.class);
        //List<Integer> distinctList = dis.all();
        //assertThat(distinctList).hasSize(0);
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
        assertThat(results)
                .hasSize(2)
                .containsKey(0)
                .containsKey(2)
                .doesNotContainKey(1);

        // Delete one with a filter as 3 matches
        // Insert 3 items
        getCollectionSimple().deleteAll();
        getCollectionSimple().insertMany(IntStream
                .range(0, 3)
                .mapToObj(i -> Document.create(i).append("indice", i).append("test", "test"))
                .collect(Collectors.toList()));
        getCollectionSimple().deleteOne(
                eq("test", "test"),
                new DeleteOneOptions().sort(descending("indice")));;
        results = getCollectionSimple()
                .find().all()
                .stream().collect(Collectors
                        .toMap(doc-> doc.getId(Integer.class), Function.identity()));
        assertThat(results).hasSize(2)
                .containsKey(0)
                .containsKey(1)
                .doesNotContainKey(2);

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
        assertThat(opt1).isPresent();
        assertThat(opt1.get()).containsEntry("hello", "world2");

        // Matched 1, modified 0, document is present
        Optional<Document> opt2 = getCollectionSimple()
                .findOneAndReplace(eq(1), new Document().id(1).append("hello", "world2"));
        assertThat(opt2).isPresent();
        assertThat(opt2.get()).containsEntry("hello", "world2");

        // Matched 0, modified 0, no document returned
        Optional<Document> opt3 = getCollectionSimple()
                .findOneAndReplace(eq(3), new Document().id(3).append("hello", "world2"),
                        new FindOneAndReplaceOptions().upsert(false));
        assertThat(opt3).isEmpty();
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
        assertThat(u2.getMatchedCount()).isZero();
        assertThat(u2.getModifiedCount()).isZero();
        assertThat(u2.getUpsertedId()).isNull();
    }

    @Test
    public void shouldTestUpdateOne() {
        // Insert a record
        getCollectionSimple().deleteAll();

        // Insert a document
        Document doc1 = new Document().id(1).append("name", "val")
                .append("price", 10.1)
                .append("field1", "value1")
                .append("test", 10.1);
        getCollectionSimple().insertOne(doc1);

        // Update the document
        UpdateResult res = getCollectionSimple().updateOne(eq(1), Update.create()
                .set(Document.create().append("name", "doe"))
                .inc("test", 1d)
                .rename("field1", "field2")
                .updateMul(Map.of("price", 1.1d)));
        assertThat(res.getMatchedCount()).isEqualTo(1);
        assertThat(res.getModifiedCount()).isEqualTo(1);
    }

    @Test
    public void shouldWorkWithProduct() {
        getCollectionVector();

        // Insert a document
        ProductString p1 = new ProductString();
        p1.setId("p1");
        p1.setName("p1");
        p1.setPrice(10.1);
        InsertOneResult res = collectionVector.insertOne(p1);
        assertThat(res).isNotNull();
        assertThat(res.getInsertedId()).isEqualTo("p1");

        // Find the retrieved object
        Optional<ProductString> optRes = collectionVector.findById("p1");
        assertThat(optRes).isPresent();
        assertThat(optRes.get().getId()).isEqualTo("p1");
    }

    @Test
    public void shouldInsertOneComplexDocument() {
        getCollectionSimple().deleteAll();
        collectionSimple.insertOne(COMPLETE_DOCUMENT);

        // Search By id
        Document res = collectionSimple.findById("1")
                .orElseThrow(() -> new IllegalStateException("Should have found a document" ));

        // Accessing result
        Instant i = res.getInstant("metadata_instant");
        Assertions.assertNotNull(i);
        Date d = res.getDate("metadata_date");
        Assertions.assertNotNull(d);
        Calendar c = res.getCalendar("metadata_calendar");
        Assertions.assertNotNull(c);
        Integer integer = res.getInteger("metadata_int");
        Assertions.assertNotNull(integer);
        Long l = res.getLong("metadata_long");
        Assertions.assertNotNull(l);
        Double db = res.getDouble("metadata_double");
        Assertions.assertNotNull(db);
        Float f = res.getFloat("metadata_float");
        Assertions.assertNotNull(f);
        String s = res.getString("metadata_string");
        Assertions.assertNotNull(s);
        Short sh = res.getShort("metadata_short");
        Assertions.assertNotNull(sh);
        Boolean b = res.getBoolean("metadata_boolean");
        Assertions.assertNotNull(b);
        UUID u = res.getUUID("metadata_uuid");
        Assertions.assertNotNull(u);
        ObjectId oid = res.getObjectId("metadata_objectId");
        Assertions.assertNotNull(oid);
        Byte by = res.getByte("metadata_byte");
        Assertions.assertNotNull(by);
        Character ch = res.getCharacter("metadata_character");
        Assertions.assertNotNull(ch);
        ProductString p = res.get("metadata_object", ProductString.class);
        Assertions.assertNotNull(p);
        List<String> l2 = res.getList("metadata_list", String.class);
        Assertions.assertNotNull(l2);
        Boolean[] ba = res.getArray("metadata_boolean_array", Boolean.class);
        Assertions.assertNotNull(ba);
    }

    @Test
    public void shouldTestFindOneAndUpdate() {
        // Insert a record
        getCollectionSimple().deleteOne(eq(1));

        // Insert a document
        Document doc1 = new Document().id(1).append("name", "val")
                .append("price", 10.1)
                .append("field1", "value1")
                .append("test", 10.1);
        getCollectionSimple().insertOne(doc1);

        // Update the document
        Optional<Document> doc = getCollectionSimple().findOneAndUpdate(eq(1), Update.create()
                        .set(Document.create().append("name", "doe"))
                        .inc("test", 1d)
                        .rename("field1", "field2")
                        .updateMul(Map.of("price", 1.1d)),
                new FindOneAndUpdateOptions().returnDocumentAfter());
        assertThat(doc).isPresent();
        assertThat(doc.get().getDouble("test")).isEqualTo(11.1d);
        assertThat(doc.get().getDouble("price")).isEqualTo(11.11d);
        assertThat(doc.get().getString("field2")).isNotNull();
    }





}
