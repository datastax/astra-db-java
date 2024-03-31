package com.datastax.astra.test.integration.collection;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.DataAPIOptions;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.admin.AstraDBAdmin;
import com.datastax.astra.client.admin.DatabaseAdmin;
import com.datastax.astra.client.exception.TooManyDocumentsToCountException;
import com.datastax.astra.client.model.CollectionOptions;
import com.datastax.astra.client.model.Command;
import com.datastax.astra.client.model.DeleteOneOptions;
import com.datastax.astra.client.model.DistinctIterable;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.FindIterable;
import com.datastax.astra.client.model.FindOneAndReplaceOptions;
import com.datastax.astra.client.model.FindOneAndUpdateOptions;
import com.datastax.astra.client.model.FindOneOptions;
import com.datastax.astra.client.model.FindOptions;
import com.datastax.astra.client.model.InsertManyOptions;
import com.datastax.astra.client.model.InsertOneResult;
import com.datastax.astra.client.model.ObjectId;
import com.datastax.astra.client.model.Projections;
import com.datastax.astra.client.model.SimilarityMetric;
import com.datastax.astra.client.model.Sorts;
import com.datastax.astra.client.model.Update;
import com.datastax.astra.client.model.UpdateResult;
import com.datastax.astra.internal.api.ApiResponse;
import com.datastax.astra.internal.command.LoggingCommandObserver;
import com.datastax.astra.test.TestConstants;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import com.dtsx.astra.sdk.utils.Utils;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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

import static com.datastax.astra.client.model.Filters.eq;
import static com.datastax.astra.client.model.Filters.gt;
import static com.datastax.astra.client.model.FindOneOptions.Builder.projection;
import static com.datastax.astra.client.model.FindOptions.Builder.sort;
import static com.datastax.astra.client.model.InsertManyOptions.Builder.concurrency;
import static com.datastax.astra.client.model.Projections.include;
import static com.datastax.astra.client.model.Sorts.ascending;
import static com.datastax.astra.client.model.Sorts.descending;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
abstract class AbstractCollectionITTest implements TestConstants {

    /** Tested Store. */
    static DatabaseAdmin apiDataAPIClient;

    static Document COMPLETE_DOCUMENT = new Document().id("1")
            .append("metadata_instant", Instant.now())
            .append("metadata_date", new Date())
            .append("metadata_calendar", Calendar.getInstance())
            .append("metadata_int", 1)
            .append("metadata_objectId", new ObjectId())
            .append("metadata_long", 1232123323L)
            .append("metadata_double", 1213.343243d)
            .append("metadata_float", 1.1232434543f)
            .append("metadata_string", "hello")
            .append("metadata_short", Short.valueOf("1"))
            .append("metadata_string_array", new String[]{"a", "b", "c"})
            .append("metadata_int_array", new Integer[]{1, 2, 3})
            .append("metadata_long_array", new Long[]{1L, 2L, 3L})
            .append("metadata_double_array", new Double[]{1d, 2d, 3d})
            .append("metadata_float_array", new Float[]{1f, 2f, 3f})
            .append("metadata_short_array", new Short[]{1, 2, 3})
            .append("metadata_boolean", true)
            .append("metadata_boolean_array", new Boolean[]{true, false, true})
            .append("metadata_uuid", UUID.randomUUID())
            .append("metadata_uuid_array", new UUID[]{UUID.randomUUID(), UUID.randomUUID()})
            .append("metadata_map", Map.of("key1", "value1", "key2", "value2"))
            .append("metadata_list", List.of("value1", "value2"))
            .append("metadata_byte", Byte.valueOf("1"))
            .append("metadata_character", 'c')
            .append("metadata_enum", AstraDBAdmin.FREE_TIER_CLOUD)
            .append("metadata_enum_array", new CloudProviderType[]{AstraDBAdmin.FREE_TIER_CLOUD, CloudProviderType.AWS})
            .append("metadata_object", new ProductString("p1", "name", 10.1));

    /**
     * Reference to working DataApiNamespace
     */
    public static Database database;

    /**
     * Initialization of the DataApiNamespace.
     *
     * @return
     *      the instance of Data ApiNamespace
     */
    protected abstract Database initDatabase();

    /**
     * Initialization of the working Namespace.
     *
     * @return
     *      current Namespace
     */
    public Database getDatabase() {
        if (database == null) {
            AbstractCollectionITTest.database = initDatabase();
        }
        return database;
    }

    public void deleteAllCollections() {
        getDatabase().dropCollection(COLLECTION_SIMPLE);
        getDatabase().dropCollection(COLLECTION_VECTOR);
        getDatabase().dropCollection(COLLECTION_ALLOW);
        getDatabase().dropCollection(COLLECTION_DENY);
        getDatabase().dropCollection(COLLECTION_UUID);
        getDatabase().dropCollection(COLLECTION_UUID_V6);
        getDatabase().dropCollection(COLLECTION_UUID_V7);
        getDatabase().dropCollection(COLLECTION_OBJECTID);
    }

    /**
     * Initialize the Test database on an Astra Environment.
     *
     * @param env
     *      target environment
     * @param cloud
     *      target cloud
     * @param region
     *      target region
     * @return
     *      the database instance
     */
    public static Database initAstraDatabase(AstraEnvironment env, CloudProviderType cloud, String region) {
        log.info("Working in environment '{}'", env.name());
        AstraDBAdmin client = getAstraDBClient(env);
        DatabaseAdmin databaseAdmin =  client.createDatabase(DATABASE_NAME, cloud, region);
        Database db = databaseAdmin.getDatabase();
        db.registerListener("logger", new LoggingCommandObserver(Database.class));
        return db;
    }

    /**
     * Access AstraDBAdmin for different environment (to create DB).
     *
     * @param env
     *      astra environment
     * @return
     *      instance of AstraDBAdmin
     */
    public static AstraDBAdmin getAstraDBClient(AstraEnvironment env) {
        switch (env) {
            case DEV:
                return DataAPIClients.createForAstraDev(Utils.readEnvVariable("ASTRA_DB_APPLICATION_TOKEN_DEV")
                                .orElseThrow(() -> new IllegalStateException("Please define env variable 'ASTRA_DB_APPLICATION_TOKEN_DEV'")))
                        .getAdmin();
            case PROD:
                return DataAPIClients.create(Utils.readEnvVariable("ASTRA_DB_APPLICATION_TOKEN")
                                .orElseThrow(() -> new IllegalStateException("Please define env variable 'ASTRA_DB_APPLICATION_TOKEN'")))
                        .getAdmin();
            case TEST:
                return DataAPIClients.createForAstraTest(Utils.readEnvVariable("ASTRA_DB_APPLICATION_TOKEN_TEST")
                                .orElseThrow(() -> new IllegalStateException("Please define env variable 'ASTRA_DB_APPLICATION_TOKEN_TEST'")))
                        .getAdmin();
            default:
                throw new IllegalArgumentException("Invalid Environment");
        }
    }

    /** Tested collection1. */
    protected static Collection<Document> collectionSimple;

    /** Tested collection2. */
    protected static Collection<ProductString> collectionVector;

    /**
     * Bean to be used for the test suite
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class Product<ID> {
        @JsonProperty("_id")
        protected ID     id;
        protected String name;
        protected Double price;
        protected UUID code;
    }

    @NoArgsConstructor
    static class ProductString extends Product<String> {
        public ProductString(String id, String name, Double price) {
            this.id = id;
            this.name = name;
            this.price = price;
        }
    }
    @NoArgsConstructor
    static class ProductObjectId extends Product<ObjectId> {}
    @NoArgsConstructor
    static class ProductObjectUUID extends Product<UUID> {}

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

    protected Collection<ProductString> getCollectionVector() {
        if (collectionVector == null) {
            collectionVector = getDatabase().createCollection(COLLECTION_VECTOR,
                    CollectionOptions
                            .builder()
                            .vectorDimension(14)
                            .vectorSimilarity(SimilarityMetric.cosine)
                            .build(), ProductString.class);
            collectionVector.registerListener("logger", new LoggingCommandObserver(Collection.class));
        }

        return collectionVector;
    }

    @Test
    @Order(1)
    public void shouldPopulateGeneralInformation() {
        assertThat(getCollectionSimple().getOptions()).isNotNull();
        assertThat(getCollectionSimple().getName()).isNotNull();
        assertThat(getCollectionSimple().getDocumentClass()).isNotExactlyInstanceOf(Document.class);
        assertThat(getCollectionSimple().getNamespaceName()).isNotNull();
        assertThat(getCollectionVector().getOptions()).isNotNull();
        assertThat(getCollectionVector().getName()).isNotNull();
        assertThat(getCollectionVector().getDocumentClass()).isNotExactlyInstanceOf(Document.class);
        assertThat(getCollectionVector().getNamespaceName()).isNotNull();
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

        ProductString product = new ProductString();
        product.setName("cool");
        product.setPrice(9.99);
        InsertOneResult res2 = getCollectionVector().insertOne(product);
        assertThat(res2).isNotNull();
        assertThat(res2.getInsertedId()).isNotNull();
    }

    @Test
    public void testFindOne() {
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
                projection(include("name")));

        // Find One with a projection only
        Optional<ProductString> doc3 = getCollectionVector().findOne(null,
                projection(include("name")));
    }

    @Test
    public void testRunCommand() {
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
    public void testCountDocument() throws TooManyDocumentsToCountException {
        InsertManyOptions.Builder.ordered(false)
                .concurrency(5) // recommended
                .chunkSize(20)  // maximum chunk size is 20
                .timeout(100);  // global timeout

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
        for(int i=11;i<1005;i+=20) {
            List<Document> docs = new ArrayList<>();
            for(int j=i;j<i+20;j++) {
                docs.add(new Document().id(j).append("indice", j));
            }
            getCollectionSimple().insertMany(docs);
        }

        // More than 1000 items
        assertThatThrownBy(() -> getCollectionSimple()
                .countDocuments(DataAPIOptions.getMaxDocumentCount()))
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
        FindOptions options = sort(ascending("indice")).skip(11).limit(2);
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
        getCollectionSimple().insertMany(docList, concurrency(5));
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
        getCollectionSimple().deleteOne(
                eq("test", "test"),
                DeleteOneOptions.Builder.sort(descending("indice")));;
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
        assertThat(optRes.isPresent()).isTrue();
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
                FindOneAndUpdateOptions.Builder.returnDocumentAfter());
        assertThat(doc).isPresent();
        assertThat(doc.get().getDouble("test")).isEqualTo(11.1d);
        assertThat(doc.get().getDouble("price")).isEqualTo(11.11d);
        assertThat(doc.get().getString("field2")).isNotNull();
    }





}
