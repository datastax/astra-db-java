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
import com.datastax.astra.client.model.FindOneOptions;
import com.datastax.astra.client.model.FindOptions;
import com.datastax.astra.client.model.InsertManyOptions;
import com.datastax.astra.client.model.InsertOneResult;
import com.datastax.astra.client.model.SimilarityMetric;
import com.datastax.astra.client.model.SortOrder;
import com.datastax.astra.client.model.Sorts;
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
import org.junit.jupiter.api.DisplayName;
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

import static com.datastax.astra.client.model.Filters.eq;
import static com.datastax.astra.client.model.Filters.gt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
abstract class AbstractCollectionITTest implements TestConstants {

    /** Tested Store. */
    static DatabaseAdmin apiDataAPIClient;

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

            database.dropCollection(COLLECTION_SIMPLE);
            database.dropCollection(COLLECTION_VECTOR);

            database.dropCollection(COLLECTION_ALLOW);
            database.dropCollection(COLLECTION_DENY);

            database.dropCollection(COLLECTION_UUID);
            database.dropCollection(COLLECTION_UUID_V6);
            database.dropCollection(COLLECTION_UUID_V7);
            database.dropCollection(COLLECTION_OBJECTID);

        }
        return database;
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

        Optional<Product> doc2 = getCollectionVector().findOne(eq(1),
                FindOneOptions.builder().projection("product_name").build());

        // Find One with a projection only
        Optional<Product> doc3 = getCollectionVector().findOne(null, FindOneOptions.builder().projection("product_name").build());
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

        InsertManyOptions.builder()
                .ordered(false)
                .withConcurrency(5) // recommended
                .withChunkSize(20)  // maximum chunk size is 20
                .withTimeout(100)   // global timeout
                .build();

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
        FindOptions options = FindOptions.builder()
                .sort(Sorts.ascending("indice"))
                .skip(11).limit(2)
                .build();
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
        getCollectionSimple().insertMany(docList, InsertManyOptions.builder().withConcurrency(5).build());
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

    @Test
    public void testVectorize() {

    }


    // ------------------------------------
    // --------- Documents   --------------
    // ------------------------------------

/*
    @Test
    @Order(15)
    @DisplayName("15. Insert Doc and find By Id")
    public void shouldInsertAndRetrieve() {
        initializeCollectionSimple();

        // Working document
        Document<Product> doc = new Document<Product>().id("p1").data(new Product("p1", 10.1));

        // Insert ONE
        DocumentMutationResult<Product> res = collectionSimple.insertOne(doc);
        Assertions.assertEquals(DocumentMutationStatus.CREATED, res.getStatus());
        Assertions.assertEquals("p1", res.getDocument().getId());
        Assertions.assertEquals("p1", res.getDocument().getData().getName());

        // Find the retrieved object
        Optional<DocumentResult<Product>> optRes = collectionSimple.findById("p1", Product.class);
        if (optRes.isPresent()) {
            DocumentResult<Product> res2 = optRes.get();
            Assertions.assertEquals("p1", res2.getId());
            Assertions.assertEquals("p1", res2.getData().getName());
        } else {
            Assertions.fail("Should have found a document");
        }
    }

    @Test
    @Order(16)
    @DisplayName("16. Insert a JsonDocument with multiple Manner")
    public void shouldInsertADocument() {
        initializeCollectionVector();

        // You must delete any existing rows with the same IDs as the
        // rows you want to insert
        collectionVector.deleteAll();

        // Insert rows defined by key/value
        collectionVector.insertOne(
                new JsonDocument()
                        .id("doc1") // uuid is generated if not explicitely set
                        .vector(new float[]{1f, 0f, 1f, 1f, 1f, 1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f})
                        .put("product_name", "HealthyFresh - Beef raw dog food")
                        .put("product_price", 12.99));

        // Insert rows defined as a JSON String
        collectionVector.insertOne(
                new JsonDocument()
                        .data(
                                "{" +
                                        "\"_id\": \"doc2\", " +
                                        "\"$vector\": [1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0], " +
                                        "\"product_name\": \"HealthyFresh - Chicken raw dog food\", " +
                                        "\"product_price\": 9.99" +
                                        "}"));

        // Insert rows defined as a Map
        collectionVector.insertOne(
                new JsonDocument()
                        .id("doc3")
                        .vector(new float[]{1f, 1f, 1f, 1f, 1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f})
                        .data(Map.of("product_name", "HealthyFresh - Chicken raw dog food")));

        // Insert rows defined as a combination of key/value, JSON, and Map
        collectionVector.insertOne(
                new JsonDocument()
                        .id("doc4")
                        .vector(new float[]{1f, 1f, 1f, 1f, 1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f})
                        .data("{" +
                                "\"product_name\": \"HealthyFresh - Chicken raw dog food\", " +
                                "\"product_price\": 8.99" +
                                "}"));

        // If you do not provide an ID, they are generated automatically
        collectionVector.insertOne(new JsonDocument().put("demo", 1));
        Assertions.assertEquals(5, collectionVector.countDocuments());
    }

    @Test
    @Order(17)
    @DisplayName("17. Insert document with a lot of properties and retrieve them")
    public void shouldInsertOneComplexDocument() {
        initializeCollectionSimple();
        Assertions.assertEquals(0, collectionSimple.countDocuments());

        // Adding Many with a Json document with a lof properties
        collectionSimple.insertOne(
                new JsonDocument().id("1")
                        .put("metadata_instant", Instant.now())
                        .put("metadata_date", new Date())
                        .put("metadata_calendar", Calendar.getInstance())
                        .put("metadata_int", 1)
                        .put("metadata_long", 12321323L)
                        .put("metadata_double", 1213.343243d)
                        .put("metadata_float", 1.1232434543f)
                        .put("metadata_string", "hello")
                        .put("metadata_short", Short.valueOf("1"))
                        .put("metadata_string_array", new String[]{"a", "b", "c"})
                        .put("metadata_int_array", new Integer[]{1, 2, 3})
                        .put("metadata_long_array", new Long[]{1L, 2L, 3L})
                        .put("metadata_double_array", new Double[]{1d, 2d, 3d})
                        .put("metadata_float_array", new Float[]{1f, 2f, 3f})
                        .put("metadata_short_array", new Short[]{1, 2, 3})
                        .put("metadata_boolean", true)
                        .put("metadata_boolean_array", new Boolean[]{true, false, true})
                        .put("metadata_uuid", UUID.randomUUID())
                        .put("metadata_uuid_array", new UUID[]{UUID.randomUUID(), UUID.randomUUID()})
                        .put("metadata_map", Map.of("key1", "value1", "key2", "value2"))
                        .put("metadata_list", List.of("value1", "value2"))
                        .put("metadata_byte", Byte.valueOf("1"))
                        .put("metadata_character", 'c')
                        .put("metadata_enum", AstraDBAdmin.FREE_TIER_CLOUD)
                        .put("metadata_enum_array", new CloudProviderType[]{AstraDBAdmin.FREE_TIER_CLOUD, CloudProviderType.AWS})
                        .put("metadata_object", new Product("name", 1d)));

        // Search By id
        JsonDocumentResult res = collectionSimple.findById("1")
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
        Byte by = res.getByte("metadata_byte");
        Assertions.assertNotNull(by);
        Character ch = res.getCharacter("metadata_character");
        Assertions.assertNotNull(ch);
        Product p = res.getObject("metadata_object", Product.class);
        Assertions.assertNotNull(p);
        List<String> l2 = res.getList("metadata_list", String.class);
        Assertions.assertNotNull(l2);
        Boolean[] ba = res.getArray("metadata_boolean_array", Boolean.class);
        Assertions.assertNotNull(ba);
    }

    // ======== UPSERT  =========

    @Test
    @Order(18)
    @DisplayName("18. UpsertOne with a jsonDocument")
    public void shouldUpsertOneWithJson()
            throws ExecutionException, InterruptedException {
        initializeCollectionSimple();
        String json = "{" +
                "\"_id\": \"doc1\", " +
                "\"$vector\": [0.3, 0.3, 0.3, 0.3, 0.3, 0.3, 0.3,0.3, 0.3, 0.3, 0.3, 0.3, 0.3, 0.3], " +
                "\"product_name\": \"HealthyFresh - Chicken raw dog food\", " +
                "\"product_price\": 9.99" +
                "}";

        // First insertion will give you a CREATED status
        JsonDocumentMutationResult res = collectionSimple.upsertOne(json);
        Assertions.assertEquals(DocumentMutationStatus.CREATED, res.getStatus());
        Assertions.assertEquals("doc1", res.getDocument().getId());

        // Second will give you  with no UNCHANGED (Async_
        res = collectionSimple.upsertOneASync(json).get();
        Assertions.assertEquals(DocumentMutationStatus.UNCHANGED, res.getStatus());

        // Third will give you  with a CHANGED
        String jsonUdated = json.replaceAll("9.99", "10.99");
        res = collectionSimple.upsertOne(jsonUdated);
        Assertions.assertEquals(DocumentMutationStatus.UPDATED, res.getStatus());

    }

    @Test
    @Order(19)
    @DisplayName("19. UpdateOne with a jsonDocument")
    public void shouldUpdate()
            throws ExecutionException, InterruptedException {
        initializeCollectionSimple();

        JsonDocument doc1 = new JsonDocument().id("1").put("a", "a").put("b", "c");
        JsonDocument doc2 = new JsonDocument().id("2").put("a", "a").put("b", "b");
        collectionSimple.insertMany(doc1, doc2);

        /*
        collectionSimple.updateOne(UpdateQuery.builder()
                .updateSet("a", "b")
                .filter(f)
                .withUpsert()
                .build());*

        collectionSimple.updateMany(UpdateQuery.builder()
                .updateSet("a", "b")
                .filter(new Filter().where("a").isEqualsTo("a"))
                .withUpsert()
                .build());
    }

    @Test
    @Order(19)
    @DisplayName("19. UpsertOne with a jsonDocument")
    public void shouldUpsertOneWithJsonDocument()
            throws ExecutionException, InterruptedException {
        initializeCollectionSimple();

        JsonDocument doc = new JsonDocument().id("1").put("a", "a").put("b", "c");
        JsonDocumentMutationResult res = collectionSimple.upsertOne(doc);
        Assertions.assertEquals(DocumentMutationStatus.CREATED, res.getStatus());
        Assertions.assertEquals("1", res.getDocument().getId());

        // Upsert with no CHANGE
        res = collectionSimple.upsertOneASync(doc).get();
        Assertions.assertEquals(DocumentMutationStatus.UNCHANGED, res.getStatus());
        Assertions.assertEquals("1", res.getDocument().getId());

        // Upsert with a CHANGE
        doc.put("b", "updated");
        res = collectionSimple.upsertOne(doc);
        Assertions.assertEquals(DocumentMutationStatus.UPDATED, res.getStatus());
        Assertions.assertEquals("1", res.getDocument().getId());
    }

    @Test
    @Order(20)
    @DisplayName("20. UpsertOne with a Document")
    public void shouldUpsertOneWithDocument()
            throws ExecutionException, InterruptedException {
        initializeCollectionSimple();

        Document<Product> doc = new Document<Product>().id("1").data(new Product("p1", 10.1));
        DocumentMutationResult<Product> res = collectionSimple.upsertOne(doc);
        Assertions.assertEquals(DocumentMutationStatus.CREATED, res.getStatus());
        Assertions.assertEquals("1", res.getDocument().getId());

        // Upsert with no CHANGE
        res = collectionSimple.upsertOneASync(doc).get();
        Assertions.assertEquals(DocumentMutationStatus.UNCHANGED, res.getStatus());
        Assertions.assertEquals("1", res.getDocument().getId());

        // Upsert with a CHANGE
        doc.getData().setName("updated");
        res = collectionSimple.upsertOne(doc);
        Assertions.assertEquals(DocumentMutationStatus.UPDATED, res.getStatus());
        Assertions.assertEquals("1", res.getDocument().getId());
    }

    // ======== INSERT MANY  =========

    @Test
    @Order(21)
    @DisplayName("21. InsertMany Json")
    public void shouldInsertManyJson() {
        initializeCollectionSimple();

        String jsonMany = "[" +
                "{\"product_name\":\"test1\",\"product_price\":12.99,\"_id\":\"doc1\"}," +
                "{\"product_name\":\"test2\",\"product_price\":2.99,\"_id\":\"doc2\"}" +
                "]";
        List<JsonDocumentMutationResult> status = collectionSimple.insertMany(jsonMany);
        Assertions.assertEquals(2, status.size());
        List<String> ids = status.stream()
                .map(JsonDocumentMutationResult::getDocument)
                .map(Document::getId)
                .collect(Collectors.toList());
        Assertions.assertTrue(ids.contains("doc1"));
        Assertions.assertTrue(ids.contains("doc2"));
        status.forEach(s -> Assertions.assertEquals(DocumentMutationStatus.CREATED, s.getStatus()));

        status = collectionSimple.insertMany(jsonMany);
        status.forEach(s -> Assertions.assertEquals(DocumentMutationStatus.ALREADY_EXISTS, s.getStatus()));

        collectionSimple.deleteAll();
        collectionSimple.insertManyASync(jsonMany).thenAccept(status2 -> {
            Assertions.assertEquals(2, status2.size());
            List<String> ids2 = status2.stream()
                    .map(JsonDocumentMutationResult::getDocument)
                    .map(Document::getId)
                    .collect(Collectors.toList());
            Assertions.assertTrue(ids2.contains("doc1"));
            Assertions.assertTrue(ids2.contains("doc2"));
            status2.forEach(s -> Assertions.assertEquals(DocumentMutationStatus.CREATED, s.getStatus()));
        });

    }

    @Test
    @Order(22)
    @DisplayName("22. InsertMany Java Bean")
    public void shouldInsertManyJavaBean() {
        initializeCollectionSimple();

        Document<Product> p1 = new Document<Product>().id("doc1").data(new Product("test1", 12.99));
        Document<Product> p2 = new Document<Product>().id("doc2").data(new Product("test2", 2.99));
        List<DocumentMutationResult<Product>> results = collectionSimple.insertMany(List.of(p1, p2));
        if (results !=null) {
            results.forEach(r -> {
                Assertions.assertEquals(DocumentMutationStatus.CREATED, r.getStatus());
                Assertions.assertNotNull(r.getDocument().getId());
                Product p = r.getDocument().getData();
                Assertions.assertNotNull(p.getName());
            });
        }

        // Same with async
        collectionSimple.deleteAll();
        collectionSimple.insertManyASync(List.of(p1, p2)).thenAccept(results2 -> {
            if (results2 !=null) {
                results2.forEach(r -> {
                    Assertions.assertEquals(DocumentMutationStatus.CREATED, r.getStatus());
                    Assertions.assertNotNull(r.getDocument().getId());
                    Product p = r.getDocument().getData();
                    Assertions.assertNotNull(p.getName());
                });
            }
        });
    }

    JsonDocument player1 = new JsonDocument().id("1").put("firstName", "Lucas").put("lastName", "Hernandez");
    JsonDocument player2 = new JsonDocument().id("2").put("firstName", "Antoine").put("lastName", "Griezmann");
    JsonDocument player3 = new JsonDocument().id("3").put("firstName", "N'Golo").put("lastName", "Kanté");
    JsonDocument player4 = new JsonDocument().id("4").put("firstName", "Paul").put("lastName", "Pogba");
    JsonDocument player5 = new JsonDocument().id("5").put("firstName", "Raphaël").put("lastName", "Varane");
    JsonDocument player6 = new JsonDocument().id("6").put("firstName", "Hugo").put("lastName", "Lloris");
    JsonDocument player7 = new JsonDocument().id("7").put("firstName", "Olivier").put("lastName", "Giroud");
    JsonDocument player8 = new JsonDocument().id("8").put("firstName", "Benjamin").put("lastName", "Pavard");
    JsonDocument player9 = new JsonDocument().id("9").put("firstName", "Kylian").put("lastName", "Mbappé");
    JsonDocument player10 = new JsonDocument().id("10").put("firstName", "Blaise").put("lastName", "Matuidi");
    JsonDocument player11 = new JsonDocument().id("11").put("firstName", "Samuel").put("lastName", "Umtiti");
    JsonDocument player12 = new JsonDocument().id("12").put("firstName", "Thomas").put("lastName", "Lemar");
    JsonDocument player13 = new JsonDocument().id("13").put("firstName", "Ousmane").put("lastName", "Dembélé");
    JsonDocument player14 = new JsonDocument().id("14").put("firstName", "Karim").put("lastName", "Benzema");
    JsonDocument player15 = new JsonDocument().id("15").put("firstName", "Adrien").put("lastName", "Rabiot");
    JsonDocument player16 = new JsonDocument().id("16").put("firstName", "Kingsley").put("lastName", "Coman");
    JsonDocument player17 = new JsonDocument().id("17").put("firstName", "Moussa").put("lastName", "Sissoko");
    JsonDocument player18 = new JsonDocument().id("18").put("firstName", "Lucas").put("lastName", "Digne");
    JsonDocument player19 = new JsonDocument().id("19").put("firstName", "Steve").put("lastName", "Mandanda");
    JsonDocument player20 = new JsonDocument().id("20").put("firstName", "Presnel").put("lastName", "Kimpembe");
    JsonDocument player21 = new JsonDocument().id("21").put("firstName", "Clement").put("lastName", "Lenglet");
    JsonDocument player22 = new JsonDocument().id("22").put("firstName", "Leo").put("lastName", "Dubois");
    JsonDocument player23 = new JsonDocument().id("23").put("firstName", "Kurt").put("lastName", "Zouma");
    JsonDocument player24 = new JsonDocument().id("24").put("firstName", "Tanguy").put("lastName", "Ndombele");

    @Test
    @Order(23)
    @DisplayName("23. InsertMany JsonDocuments")
    public void shouldManyJsonDocuments() {
        initializeCollectionSimple();
        collectionSimple.insertManyJsonDocuments(List.of(player1, player2)).forEach(r -> {
            Assertions.assertEquals(DocumentMutationStatus.CREATED, r.getStatus());
            Assertions.assertNotNull(r.getDocument().getId());
            Assertions.assertNotNull(r.getDocument().getString("firstName"));
        });
        collectionSimple.deleteAll();

        // Same but Async
        collectionSimple.insertManyJsonDocumentsASync(List.of(player1, player2)).thenAccept(r -> {
            Assertions.assertEquals(2, r.size());
            r.forEach(res -> {
                Assertions.assertEquals(DocumentMutationStatus.CREATED, res.getStatus());
                Assertions.assertNotNull(res.getDocument().getId());
                Assertions.assertNotNull(res.getDocument().getString("firstName"));
            });
        });
    }

    @Test
    @Order(24)
    @DisplayName("24. InsertMany too many items")
    public void shouldInsertTooMany() {
        initializeCollectionSimple();
        //Assertions.assertThrows(DataApiInvalidArgumentException.class,
        //        () -> collectionSimple.insertMany( List.of(
        //                player1, player2, player3, player4, player5, player6,
        //                player7, player8, player9, player10,player11, player12,
        //                player13, player14, player15, player16, player17, player18,
        //                player19, player20, player21, player22, player23, player24)));
        try {
            collectionSimple.insertMany( List.of(
                    player1, player2, player3, player4, player5, player6,
                    player7, player8, player9, player10,player11, player12,
                    player13, player14, player15, player16, player17, player18,
                    player19, player20, player21, player22, player23, player24));
        } catch(DataApiInvalidArgumentException dai) {
            dai.printStackTrace();;
        }
    }


    @Test
    @Order(25)
    @DisplayName("25. InsertMany order true, no replace")
    public void shouldInsertManyOrdered() {
        initializeCollectionSimple();

        List<Document<Map<String, Object>>> othersPlayers = new ArrayList<>(List.of(
                player1, player2, player3,
                player4, player5, player6,
                player7, player8, player9));
        List<DocumentMutationStatus> statuses1 = new ArrayList<>();
        collectionSimple.insertMany(othersPlayers).forEach(res -> {
            Assertions.assertNotNull(res.getDocument().getId());
            Assertions.assertEquals(DocumentMutationStatus.CREATED, res.getStatus());
            statuses1.add(res.getStatus());
        });
        log.info("Statuses => " + statuses1.stream().map(Enum::name).collect(Collectors.joining(", ")));

        // Insert again
        List<DocumentMutationStatus> statuses = new ArrayList<>();
        collectionSimple.enableOrderingWhenInsert();
        collectionSimple.insertMany(othersPlayers).forEach(res -> {
            Assertions.assertNotNull(res.getDocument().getId());
            if (player1.getId().equals(res.getDocument().getId())) {
                Assertions.assertEquals(DocumentMutationStatus.ALREADY_EXISTS, res.getStatus());
            } else {
                Assertions.assertEquals(DocumentMutationStatus.NOT_PROCESSED, res.getStatus());
            }
            statuses.add(res.getStatus());
        });
        log.info("Statuses => " + statuses.stream().map(Enum::name).collect(Collectors.joining(", ")));
    }

    @Test
    @Order(26)
    @DisplayName("26. InsertMany with replacements")
    public void shouldInsertManyWithDuplicatesOrder() {
        initializeCollectionSimple();

        List<Document<Map<String, Object>>> othersPlayers = new ArrayList<>(List.of(
                player1, player2, player3,
                player4, player5, player6,
                player7, player8));
        othersPlayers.add(new JsonDocument().id("9").put("firstName", "Kylian2").put("lastName", "Mbappé"));
        othersPlayers.addAll(List.of(player9, player10,player11));
        log.info("Players order, 9 is duplicate :" + othersPlayers
                .stream().map(Document::getId)
                .collect(Collectors.joining(", ")));


        // Status CREATED up to the duplicate
        List<DocumentMutationStatus> statuses = new ArrayList<>();
        collectionSimple.enableOrderingWhenInsert();
        collectionSimple.insertMany(othersPlayers).forEach(res -> {
            Assertions.assertNotNull(res.getDocument().getId());
            int id = Integer.parseInt(res.getDocument().getId());
            if (id<9) {
                Assertions.assertEquals(DocumentMutationStatus.CREATED, res.getStatus());
            } else if (id==9) {
                Assertions.assertEquals(DocumentMutationStatus.ALREADY_EXISTS, res.getStatus());
            } else {
                Assertions.assertEquals(DocumentMutationStatus.NOT_PROCESSED, res.getStatus());
            }
            statuses.add(res.getStatus());
        });
        log.info("Statuses1 => " + statuses.stream().map(Enum::name).collect(Collectors.joining(", ")));

        // Status ALREADY EXIST for first and else NOT PROCESS
        List<DocumentMutationStatus> statuses2 = new ArrayList<>();
        collectionSimple.insertMany(othersPlayers).forEach(res -> {
            Assertions.assertNotNull(res.getDocument().getId());
            int id = Integer.parseInt(res.getDocument().getId());
            if (id==1) {
                Assertions.assertEquals(DocumentMutationStatus.ALREADY_EXISTS, res.getStatus());
            } else {
                Assertions.assertEquals(DocumentMutationStatus.NOT_PROCESSED, res.getStatus());
            }
            statuses2.add(res.getStatus());
        });
        log.info("Statuses2 => " + statuses2.stream().map(Enum::name).collect(Collectors.joining(", ")));

        // 1 to 9 is ALREADY_EXIST, 10 and 11 are created
        collectionSimple.disableOrderingWhenInsert();
        List<DocumentMutationStatus> statuses3 = new ArrayList<>();
        collectionSimple.insertMany(othersPlayers).forEach(res -> {
            Assertions.assertNotNull(res.getDocument().getId());
            int id = Integer.parseInt(res.getDocument().getId());
            if (id<10) {
                Assertions.assertEquals(DocumentMutationStatus.ALREADY_EXISTS, res.getStatus());
            } else {
                Assertions.assertEquals(DocumentMutationStatus.CREATED, res.getStatus());
            }
            statuses3.add(res.getStatus());
        });
        log.info("Statuses3 => " + statuses3.stream().map(Enum::name).collect(Collectors.joining(", ")));

        // Try to replace
        List<DocumentMutationStatus> statuses4 = new ArrayList<>();
        collectionSimple.upsertMany(othersPlayers).forEach(res -> {
            Assertions.assertNotNull(res.getDocument().getId());
            int id = Integer.parseInt(res.getDocument().getId());
            if (id==9) {
                Assertions.assertEquals(DocumentMutationStatus.UPDATED, res.getStatus());
            } else {
                Assertions.assertEquals(DocumentMutationStatus.UNCHANGED, res.getStatus());
            }
            statuses4.add(res.getStatus());
        });
        log.info("Statuses4 => " + statuses4.stream().map(Enum::name).collect(Collectors.joining(", ")));
    }

    // ======== INSERT MANY =========

    @Test
    @Order(27)
    @DisplayName("27. InsertVeryMany Documents")
    public void shouldInsertManyChunkedSequential() {
        initializeCollectionSimple();

        int nbDocs = 251;
        List<Document<Product>> documents = new ArrayList<>();
        long start = System.currentTimeMillis();
        for (int i = 0; i < nbDocs; i++) {
            documents.add(new Document<Product>().id(String.valueOf(i)).data(new Product("Desc " + i, i * 1.0d)));
        }
        List<DocumentMutationResult<Product>> result = collectionSimple.insertManyChunked(documents, 20, 1);
        long end = System.currentTimeMillis();
        log.info("Inserting {} documents took {} ms", nbDocs, end - start);
        Assertions.assertEquals(nbDocs, collectionSimple.countDocuments());
        Assertions.assertEquals(nbDocs, result.size());
        collectionSimple.deleteAll();
        collectionSimple
                .insertManyChunkedASync(documents, 20, 1)
                .thenAccept(res -> Assertions.assertEquals(nbDocs, res.size()));
    }

    @Test
    @Order(28)
    @DisplayName("28. InsertVeryMany concurrently")
    public void shouldInsertManyChunkedParallel() {
        initializeCollectionSimple();
        List<Document<Product>> documents = new ArrayList<>();
        long start = System.currentTimeMillis();

        int nbDocs = 999;
        for (int i = 0; i < nbDocs; i++) {
            documents.add(new Document<Product>().id(String.valueOf(i)).data(new Product("Desc " + i, i * 1.0d)));
        }
        collectionSimple.insertManyChunked(documents, 20, 20);
        long end = System.currentTimeMillis();
        log.info("Inserting {} documents took {} ms", nbDocs, end - start);

        collectionSimple.countDocuments();

        long top = System.currentTimeMillis();
        //DeleteQuery query = DeleteQuery.builder()
        //        .where("product_price", GREATER_THAN, 100)
        //        .build();
        //collectionSimple.deleteMany(query);
        //collectionSimple.deleteManyChunked(query, 5);
        //System.out.println("Total time " + (System.currentTimeMillis() - top));
        //collectionSimple.countDocuments();

        /*
        collectionSimple.insertManyChunkedASync(documents, 20, 20).thenAccept(res -> {
            Assertions.assertEquals(nbDocs, res.size());
            Assertions.assertEquals(nbDocs, collectionSimple.countDocuments());
        });*

    }

    @Test
    @Order(29)
    @DisplayName("29. InsertMany with duplicates")
    public void insertWithDuplicatesLeadToErrors() {
        initializeCollectionSimple();
        collectionSimple.enableOrderingWhenInsert();
        List<JsonDocumentMutationResult> status = collectionSimple.insertManyJsonDocuments(List.of(
                new JsonDocument().id("1").put("firstName", "Kylian").put("lastName", "Mbappé"),
                new JsonDocument().id("1").put("firstName", "Antoine").put("lastName", "Griezmann")));
        Assertions.assertEquals(DocumentMutationStatus.ALREADY_EXISTS, status.get(0).getStatus());
    }

    @Test
    @Order(30)
    @DisplayName("30. UpsertMany")
    public void insertVeryWithDuplicatesLeadToErrors() {
        initializeCollectionSimple();
        List<JsonDocumentMutationResult> status = collectionSimple.upsertManyJsonDocuments(List.of(player1, player2, player3));
        Assertions.assertEquals(DocumentMutationStatus.CREATED, status.get(0).getStatus());
    }

    // ======== FIND =========

    @Test
    @Order(25)
    @DisplayName("25. Find with $gte")
    public void shouldFindWithGreaterThan() {
        shouldInsertADocument();
        Assertions.assertEquals(1, collectionVector.find(SelectQuery.builder()
                .filter(new Filter().where("product_price")
                        .isGreaterOrEqualsThan(12.99))
                .build()).count());
    }

    @Test
    @Order(25)
    @DisplayName("25. Find with $gte")
    public void shouldFindWithEquals() {
        shouldInsertADocument();
        /*
        Filter f1 = new Filter().where("product_price").isEqualsTo(12.99);
        Assertions.assertEquals(1, collectionVector.find(new SelectQuery(f1)).count());

        Filter f2 = new Filter().
                and()
                  .where("product_price", FilterOperator.EQUALS_TO,12.99)
                  .where("product_name", FilterOperator.EQUALS_TO, "HealthyFresh - Beef raw dog food")
                .end();
        Assertions.assertEquals(1, collectionVector.find(new SelectQuery(f2)).count());
        Filter f3 = new Filter()
                .not()
                  .where("product_price", FilterOperator.EQUALS_TO,10.99)
                .end();
        Assertions.assertEquals(5, collectionVector.find(new SelectQuery(f3)).count());

        Filter f4 = new Filter("{\"$not\":{\"product_price\":{\"$eq\":10.99}}}");
        Assertions.assertEquals(5, collectionVector.find(new SelectQuery(f4)).count());
        *

        Filter yaFilter = new Filter()
                .and()
                .or()
                .where("a", EQUALS_TO, 10)
                .where("b", EXISTS, true)
                .end()
                .or()
                .where("c", GREATER_THAN, 5)
                .where("d", GREATER_THAN_OR_EQUALS_TO, 5)
                .end()
                .not()
                .where("e", LESS_THAN, 5)
                .end();

        collectionVector.find(new SelectQuery(yaFilter));

    }

    @Test
    @Order(26)
    @DisplayName("26. Find with $gt")
    // Greater than
    public void shouldFindGreaterThan() {
        shouldInsertADocument();
        Assertions.assertEquals(1, collectionVector.find(SelectQuery.builder()
                .filter(new Filter().where("product_price")
                        .isGreaterThan(10))
                .build()).count());
    }

    @Test
    @Order(27)
    @DisplayName("27. Find with $lt (less than)")
    // Greater than
    public void shouldFindLessThen() {
        shouldInsertADocument();
        Assertions.assertEquals(2, collectionVector.find(SelectQuery.builder()
                .filter(new Filter().where("product_price")
                        .isLessThan(10))
                .build()).count());
    }

    @Test
    @Order(28)
    @DisplayName("28. Find with $lte (less than or equals)")
    // Greater than
    public void shouldFindLessOrEqualsThen() {
        shouldInsertADocument();
        Assertions.assertEquals(2, collectionVector.find(SelectQuery.builder()
                .filter(new Filter().where("product_price")
                        .isLessOrEqualsThan(9.99))
                .build()).count());
    }

    @Test
    @Order(29)
    @DisplayName("29. Find with $eq")
    // Greater than
    public void shouldEqualsThen() {
        shouldInsertADocument();
        Assertions.assertEquals(1, collectionVector.find(SelectQuery.builder()
                .filter(new Filter().where("product_price")
                        .isEqualsTo(9.99))
                .build()).count());
    }

    @Test
    @Order(30)
    @DisplayName("30. Find Nwith $ne (not equals)")
    // Greater than
    public void shouldNotEqualsThen() {
        shouldInsertADocument();
        Assertions.assertEquals(4, collectionVector.find(SelectQuery.builder()
                .filter(new Filter().where("product_price")
                        .isNotEqualsTo(9.99))
                .build()).count());
    }

    @Test
    @Order(31)
    @DisplayName("31. Find with $exists")
    // Greater than
    public void shouldFindExists() {
        shouldInsertADocument();
        Assertions.assertEquals(3, collectionVector.find(SelectQuery.builder()
                .filter(new Filter().where("product_price")
                        .exists())
                .build()).count());
    }

    @Test
    @Order(32)
    @DisplayName("32. AND with Exists and Not Equals")
    // Greater than
    public void shouldFindAndExistsAndNotEquals() {
        shouldInsertADocument();
        // Exists AND not equals
        // {"find":{"filter":{"$and":[{"product_price":{"$exists":true}},{"product_price":{"$ne":9.99}}]}}}
        SelectQuery existAndNotEquals = new SelectQuery();
        List<Map<String, Map<String, Object>>> andCriteriaList = new ArrayList<>();
        Map<String, Map<String, Object>> criteria1 = new HashMap<>();
        criteria1.put("product_price", Map.of("$exists", true));
        Map<String, Map<String, Object>> criteria2 = new HashMap<>();
        criteria2.put("product_price", Map.of("$ne", 9.99));
        andCriteriaList.add(criteria1);
        andCriteriaList.add(criteria2);
        existAndNotEquals.setFilter(Map.of("$and", andCriteriaList));
        Assertions.assertEquals(2, collectionVector.find(existAndNotEquals).count());

        SelectQuery query2 = SelectQuery.builder().filter(new Filter("{" +
                        "\"$and\":[" +
                        "   {" +
                        "\"product_price\": {\"$exists\":true}" +
                        "}," +
                        "{" +
                        "\"product_price\":{\"$ne\":9.99}}]" +
                        "}"))
                .build();
        Assertions.assertEquals(2, collectionVector.find(query2).count());

    }

    @Test
    @Order(33)
    @DisplayName("33. Find $in")
    public void shouldFindWithIn() {
        shouldInsertOneComplexDocument();

        // $in
        log.info("Search with $in...");
        Assertions.assertTrue(collectionSimple.find(SelectQuery.builder()
                        .filter(new Filter().where("metadata_string")
                                .isInArray(new String[]{"hello", "world"})).build())
                .findFirst().isPresent());
    }

    @Test
    @Order(34)
    @DisplayName("34. Find $nin")
    public void shouldFindWithNIn() {
        shouldInsertOneComplexDocument();
        Assertions.assertTrue(collectionSimple.find(SelectQuery.builder()
                        .filter(new Filter().where("metadata_string")
                                .isNotInArray(new String[]{"Hallo", "Welt"})).build())
                .findFirst().isPresent());
    }

    @Test
    @Order(35)
    @DisplayName("35. Should find with $size")
    public void shouldFindWithSize() {
        shouldInsertOneComplexDocument();
        Assertions.assertTrue(collectionSimple.find(SelectQuery.builder()
                .filter(new Filter().where("metadata_boolean_array")
                        .hasSize(3)).build()).findFirst().isPresent());
    }

    @Test
    @Order(36)
    @DisplayName("36. Should find with $lt")
    public void shouldFindWithLT() {
        shouldInsertOneComplexDocument();
        Assertions.assertTrue(collectionSimple.find(SelectQuery.builder()
                .filter(new Filter().where("metadata_int")
                        .isLessThan(2)).build()).findFirst().isPresent());
    }

    @Test
    @Order(37)
    @DisplayName("37. Should find with $lte")
    public void shouldFindWithLTE() {
        shouldInsertOneComplexDocument();
        Assertions.assertTrue(collectionSimple.find(SelectQuery.builder()
                .filter(new Filter().where("metadata_int")
                        .isLessOrEqualsThan(1)).build()).findFirst().isPresent());
    }

    @Test
    @Order(38)
    @DisplayName("38. Should find with $gt")
    public void shouldFindWithGTE() {
        shouldInsertOneComplexDocument();
        Assertions.assertTrue(collectionSimple.find(
                SelectQuery.builder().filter(new Filter()
                        .where("metadata_int")
                        .isGreaterThan(0)).build()).findFirst().isPresent());
    }

    @Test
    @Order(39)
    @DisplayName("39. Should find with $gte and Instant")
    public void shouldFindWithGTEInstant() {
        shouldInsertOneComplexDocument();
        Assertions.assertTrue(collectionSimple.find(SelectQuery.builder().filter(new Filter()
                .where("metadata_instant")
                .isLessThan(Instant.now())).build()).findFirst().isPresent());
    }

    @Test
    @Order(40)
    @DisplayName("40. ToString should provide the json String")
    public void shouldSerializedAsJson() {

        // Serializing a JsonDocument give you back the Json String
        JsonDocument doc1 = new JsonDocument().id("1").put("a", "a").put("b", "c");
        Assertions.assertEquals("{\"a\":\"a\",\"b\":\"c\",\"_id\":\"1\"}", doc1.toString());

        // Serializing a Document<T> give you back a Json String
        Document<Product> doc2 = new Document<Product>().id("1").data(new Product("name", 1d));
        Assertions.assertEquals("{\"product_name\":\"name\",\"product_price\":1.0,\"_id\":\"1\"}", doc2.toString());

        initializeCollectionVector();
        collectionVector.insertManyJsonDocuments(List.of(
                new JsonDocument()
                        .id("doc1") // generated if not set
                        .vector(new float[]{1f, 0f, 1f, 1f, 1f, 1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f})
                        .put("product_name", "HealthyFresh - Beef raw dog food")
                        .put("product_price", 12.99),
                new JsonDocument()
                        .id("doc2")
                        .vector(new float[]{1f, 1f, 1f, 1f, 1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f})
                        .data("{\"product_name\": \"HealthyFresh - Chicken raw dog food\", \"product_price\": 9.99}"),
                new JsonDocument()
                        .id("doc3")
                        .vector(new float[]{1f, 1f, 1f, 1f, 1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f})
                        .data(Map.of("product_name", "HealthyFresh - Chicken raw dog food")),
                new JsonDocument()
                        .id("doc4")
                        .vector(new float[]{1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f})
                        .put("product_name", "HealthyFresh - Chicken raw dog food")
                        .put("product_price", 9.99)
        ));

        SelectQuery query2 = SelectQuery.builder()
                .orderByAnn(new float[]{1f, 1f, 1f, 1f, 1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f})
                .withLimit(4)
                .includeSimilarity()
                .build();
        collectionVector.find(query2).forEach(System.out::println);
    }

    @Test
    @Order(41)
    @DisplayName("41. Create Collections (with deny)")
    public void shouldCreateCollectionWithDenyOptions() {
        if (astraDbAdmin == null) shouldConnectToDatabase();
        // When
        collectionDeny = astraDB.createCollection(CollectionDefinition.builder()
                .name(TEST_COLLECTION_DENY)
                .vector(14, SimilarityMetric.cosine)
                .indexingDeny("blob_body")
                .build());
        collectionDeny.insertOne(new JsonDocument()
                .id("p1")
                .put("prop1", "value1")
                .put("blob_body", "hello"));
        // Then
        Assertions.assertTrue(collectionDeny
                .findById("p1").isPresent());
        Assertions.assertTrue(collectionDeny
                .findOne(SelectQuery
                        .builder().filter(new Filter().where("prop1")
                                .isEqualsTo("value1")).build()).isPresent());
        Assertions.assertThrows(DataApiException.class, () -> collectionDeny
                .findOne(SelectQuery.builder()
                        .filter(new Filter().where("blob_body")
                                .isEqualsTo("hello"))
                        .build()));
    }

    @Test
    public void shouldDoSemanticSearch() {
        if (astraDbAdmin == null) shouldConnectToDatabase();
        initializeCollectionVector();

        // When
        // Insert vectors
        collectionVector.insertOne(
                new JsonDocument()
                        .id("doc1") // generated if not set
                        .vector(new float[]{1f, 0f, 1f, 1f, 1f, 1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f})
                        .put("product_name", "HealthyFresh - Beef raw dog food")
                        .put("product_price", 12.99));
        collectionVector.insertOne(
                new JsonDocument()
                        .id("doc2")
                        .vector(new float[]{1f, 1f, 1f, 1f, 1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f})
                        .put("product_name", "HealthyFresh - Chicken raw dog food")
                        .put("product_price", 9.99));
        collectionVector.insertOne(
                new JsonDocument()
                        .id("doc3")
                        .vector(new float[]{1f, 1f, 1f, 1f, 1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f})
                        .data(Map.of("product_name", "HealthyFresh - Chicken raw dog food")));
        collectionVector.insertOne(
                new JsonDocument()
                        .id("doc4")
                        .vector(new float[]{1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f})
                        .put("product_name", "HealthyFresh - Chicken raw dog food")
                        .put("product_price", 9.99));

        // Perform a similarity search
        float[] embeddings = new float[] {1f, 1f, 1f, 1f, 1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f};
        //Filter metadataFilter = new Filter().where("product_price").isEqualsTo(9.99);
        int maxRecord = 10;
        long top = System.currentTimeMillis();
        Stream<JsonDocumentResult> resultsSet = collectionVector.findVector(embeddings, null, maxRecord);
        System.out.println(System.currentTimeMillis() - top);
    }

    @Test
    @Order(42)
    @DisplayName("42. Create Collections (with allow)")
    public void shouldCreateCollectionWithAllowOptions() {
        if (astraDbAdmin == null) shouldConnectToDatabase();
        // ---- TESTING WITH ALLOW -----

        // When
        astraDB.deleteCollection(TEST_COLLECTION_ALLOW);
        collectionAllow = astraDB.createCollection(CollectionDefinition.builder()
                .name(TEST_COLLECTION_ALLOW)
                .vector(14, SimilarityMetric.cosine)
                .indexingAllow("prop1")
                .build());
        collectionAllow.insertOne(new JsonDocument()
                .id("p1")
                .put("prop1", "value1")
                .put("blob_body", "hello"));
        Assertions.assertTrue(collectionAllow
                .findOne(SelectQuery.builder() .filter(new Filter().where("prop1").isEqualsTo("value1"))
                        .build()).isPresent());
        Assertions.assertThrows(DataApiException.class, () -> collectionAllow
                .findOne(SelectQuery.builder()
                        .filter(new Filter().where("blob_body")
                                .isEqualsTo("hello"))
                        .build()));

    }

    @Test
    @Order(43)
    @DisplayName("43. Find in array (not keyword)")
    public void testFindInArray() {
        initializeCollectionSimple();
        // Given 2 records
        collectionSimple.insertManyJsonDocuments(List.of(
                new JsonDocument().id("1").put("names", List.of("John", "Doe")),
                new JsonDocument().id("2").put("names", List.of("Cedrick", "Lunven"))
        ));
        // I should perform an any filter in a collection
        Assertions.assertEquals(1, collectionSimple.find(SelectQuery.builder()
                .filter(new Filter().where("names")
                        .isEqualsTo("John"))
                .build()).count());
    }

    // ----------------------------------------
    // --------- Object Mapping ---------------
    // ----------------------------------------

    static AstraDBRepository<Product> productRepositoryVector;
    static AstraDBRepository<Product> productRepositorySimple;

    @Test
    @Order(50)
    @DisplayName("50. Insert with CollectionRepository and vector")
    public void shouldInsertRecords() {
        initializeCollectionVector();

        productRepositoryVector = astraDB.getCollection(TEST_COLLECTION_VECTOR, Product.class);
        productRepositoryVector.insert(new Document<>(
                "product1",
                new Product("something Good", 9.99),
                new float[]{1f, 0f, 1f, 1f, 1f, 1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f}));

        // Add vector without an id
        productRepositoryVector.insert(new Document<Product>()
                .data(new Product("id will be generated for you", 10.99))
                .vector(new float[]{1f, 0f, 1f, 1f, 1f, 1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f}));

        // Insert a full-fledged object
        productRepositoryVector.insert(new Document<Product>()
                .id("pf2000")
                .vector(new float[]{1f, 0f, 1f, 1f, 1f, 1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f}));
    }

    @Test
    @Order(51)
    @DisplayName("51. Insert with CollectionRepository")
    public void shouldInsertWithSimpleCollectionObjectMapping() {
        productRepositorySimple = astraDB.getCollection(TEST_COLLECTION_NAME, Product.class);
        Assertions.assertNotNull(productRepositorySimple);
        productRepositorySimple.save(new Document<Product>().id("p1").data(new Product("Pupper Sausage Beef dog Treats", 9.99)));
        productRepositorySimple.save(new Document<Product>().id("p2").data(new Product("Dog Ring Chew Toy", 10.99)));
        productRepositorySimple.saveAll(List.of(
                new Document<Product>().id("p3").data(new Product("Dog Ring Chew Toy", 9.99)),
                new Document<Product>().id("p4").data(new Product("Pepper Sausage Bacon dog Treats", 9.99))
        ));
    }

    private void initializeCollectionSimple() {
        if (astraDbAdmin == null) {
            databaseId = astraDbAdmin.createDatabase(TEST_DBNAME, targetCloud, targetRegion);
            astraDB = astraDbAdmin.getDatabase(databaseId);
        }
        if (collectionSimple == null) {
            collectionSimple = astraDB.createCollection(TEST_COLLECTION_NAME);
        }
        collectionSimple.deleteAll();
    }

    private void initializeCollectionVector() {
        if (astraDbAdmin == null) {
            databaseId = astraDbAdmin.createDatabase(TEST_DBNAME, targetCloud, targetRegion);
            astraDB = astraDbAdmin.getDatabase(databaseId);
        }
        if (collectionVector == null) {
            collectionVector = astraDB.createCollection(TEST_COLLECTION_VECTOR, 14);
        }
        collectionVector.deleteAll();
    }*/

}
