package com.datastax.astra.test.integration.prod;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.commands.Update;
import com.datastax.astra.client.collections.commands.Updates;
import com.datastax.astra.client.collections.commands.options.CollectionUpdateOneOptions;
import com.datastax.astra.client.collections.commands.results.CollectionUpdateResult;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Filters;
import com.datastax.astra.client.databases.Database;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

public class AstraProdCollectionEvolutionTest {

    public static final String ASTRA_TOKEN = System.getenv("ASTRA_DB_APPLICATION_TOKEN");

    public static final String ASTRA_DB_ENDPOINT = "https://7d7388a6-5ba2-431a-942a-250012f785c0-us-east1.apps.astra.datastax.com";
    public static final String COLLECTION_NAME = "sample_nested";

    private  Collection<Document> getCollection(boolean clear) {
        DataAPIClient        client = DataAPIClients.astra(ASTRA_TOKEN);//new DataAPIClient(ASTRA_TOKEN);
        Database             db     = client.getDatabase(ASTRA_DB_ENDPOINT);
        Collection<Document>  ccc = db.createCollection(COLLECTION_NAME);
        if (clear) ccc.deleteAll();
        return ccc;
    }

    @Test
    public void shouldRetrieveNestedProperties() {
        Collection<Document> ccc = getCollection(false);

        String sampleId = "001";
        // Our Test document does not exist
        if (ccc.findById(sampleId).isEmpty()) {
            Map<String,String> lvl2 = Map.of("a", "a", "b", "b");
            Document doc1 = new Document(sampleId)
                    .append("field1", "hello")
                    .append("field2", List.of("alpha", "beta", "gamma"))
                    .append("field3", List.of(lvl2, lvl2))
                    .append("metadata", Map.of(
                            "key1", "value1",
                            "key2", lvl2,
                            "key3", List.of(1, 2, 3)));
            ccc.insertOne(doc1);
        }

        Document myDoc = ccc.findById(sampleId).get();
        Assertions.assertNotNull(myDoc);
        // Navigating Arrays
        Assertions.assertNotNull(myDoc.get("field2"));
        Assertions.assertEquals(ArrayList.class, myDoc.get("field2").getClass());

        Assertions.assertNotNull(myDoc.get("field2[0]"));
        Assertions.assertEquals("alpha", myDoc.get("field2[0]"));
        Assertions.assertNotNull(myDoc.get("field2[1]"));
        Assertions.assertEquals("beta", myDoc.get("field2[1]"));
        Assertions.assertNull(myDoc.get("field2[3]"));
        Assertions.assertNull(myDoc.get("field2[-1]"));

        // Navigating Maps level1
        Assertions.assertNotNull(myDoc.get("metadata"));
        Assertions.assertEquals(LinkedHashMap.class, myDoc.get("metadata").getClass());
        Assertions.assertNotNull(myDoc.get("metadata.key1"));
        Assertions.assertNotNull(myDoc.get("metadata.key2"));

        // Nested Arrays
        Assertions.assertNotNull(myDoc.get("metadata.key3[2]"));
        Assertions.assertEquals(3, myDoc.get("metadata.key3[2]"));

        // Nested Map
        Assertions.assertNotNull(myDoc.get("metadata.key2.a"));
        Assertions.assertEquals("a", myDoc.get("metadata.key2.a"));

        Assertions.assertNotNull(myDoc.get("field3[0].b"));
        Assertions.assertEquals("b", myDoc.get("field3[0].b"));
    }

    @Test
    public void shouldAppendWithDotNotation() {
        Document doc = new Document();
        doc.append("field1", "hello");
        doc.append("metadata.key1", "value1");
        // Adding a nested structure
        doc.append("metadata.key2.key11", "value11");
        Assertions.assertNotNull(((Map<?, ?>)(doc.get("metadata"))).get("key2"));
        Assertions.assertNotNull(doc.get("metadata.key2.key11"));
        System.out.println(doc.toJson());

        // Contain KEY
        Assertions.assertTrue(doc.containsKey("metadata.key2"));
        Assertions.assertTrue(doc.containsKey("metadata.key2.key11"));
        Assertions.assertFalse(doc.containsKey("metadata.key2.key12"));

        // Remove
        doc.remove("metadata.key2.key11");
        Assertions.assertFalse(doc.containsKey("metadata.key2.key11"));
        System.out.println(doc.toJson());
    }

    @Test
    public void shouldWorkWithDistinct() {
        Collection<Document> ccc = getCollection(true);
        List<Document> animal = new ArrayList<>();
        animal.add(new Document("1")
                .append("name", "Kittie")
                .append("metadata", Map.of("animal", "cat", "color", "black")));
        animal.add(new Document("2")
                .append("name", "Lassie")
                .append("metadata", Map.of("animal", "dog", "breed", "Rough Collie", "color", "brown and white")));
        animal.add(new Document("3")
                        .append("name", "Dolly")
                        .append("metadata", Map.of("animal", "sheep", "breed", "Finn-Dorset", "cloned", "yes")));
        animal.add(new Document("4")
                        .append("name", "Marjan")
                        .append("metadata", Map.of("animal", "lion", "location", "Kabul Zoo", "fame", "war survivor")));
        animal.add(new Document("5")
                        .append("name", "Clever Hans")
                        .append("metadata", Map.of("animal", "horse", "fame", "math abilities")));
        animal.add(new Document("6")
                        .append("name", "Paul")
                        .append("metadata", Map.of("animal", "octopus", "fame", "World Cup predictions")));
        animal.add(new Document("7")
                        .append("name", "Hachiko")
                        .append("metadata", Map.of("animal", "dog", "breed", "Akita", "fame", "loyalty")));
        animal.add(new Document("8")
                        .append("name", "Balto")
                        .append("metadata", Map.of("animal", "dog", "breed", "Siberian Husky", "fame", "serum run hero")));
        animal.add(new Document("9")
                        .append("name", "Babe")
                        .append("metadata", Map.of("animal", "pig", "fame", "movie star")));
        animal.add(new Document("10")
                        .append("name", "Togo")
                        .append("metadata", Map.of("animal", "dog", "breed", "Siberian Husky", "fame", "real serum run hero")));
        ccc.insertMany(animal);

        Set<String> races = ccc.distinct("metadata.animal", String.class);
        System.out.println(races);
    }

    @Test
    public void shouldNotErrorOnUpdates() {
        Collection<Document> ccc = getCollection(true);
        ccc.insertOne(new Document("1")
                .append("isCheckedOut", true)
               .append("numberOfPages", 1)
                .append("color", "blue"));

        Filter filter = Filters.and(
                Filters.eq("isCheckedOut", false),
                Filters.lt("numberOfPages", 10));
        Update update = Updates.set("color", "yellow");
        CollectionUpdateOneOptions options = new CollectionUpdateOneOptions().upsert(true);
        CollectionUpdateResult result = ccc.updateOne(filter, update, options);
        System.out.println(result.getMatchedCount());
        System.out.println(result.getModifiedCount());
        System.out.println(result.getUpsertedId());

    }



}
