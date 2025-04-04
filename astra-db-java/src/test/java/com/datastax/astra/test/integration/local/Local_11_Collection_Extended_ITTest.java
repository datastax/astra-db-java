package com.datastax.astra.test.integration.local;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.commands.Update;
import com.datastax.astra.client.collections.commands.Updates;
import com.datastax.astra.client.collections.commands.options.CollectionFindOneOptions;
import com.datastax.astra.client.collections.commands.options.CollectionUpdateOneOptions;
import com.datastax.astra.client.collections.commands.results.CollectionUpdateResult;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Filters;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.exceptions.InvalidFieldExpressionException;
import com.datastax.astra.internal.utils.EscapeUtils;
import com.datastax.astra.test.integration.AbstractDataAPITest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.*;

@EnabledIfSystemProperty(named = "ASTRA_DB_JAVA_TEST_ENV", matches = "local")
public class Local_11_Collection_Extended_ITTest extends AbstractDataAPITest {

    protected  Collection<Document> getCollection(boolean clear) {
        Collection<Document>  ccc = getDatabase().createCollection("extended_names");
        if (clear) ccc.deleteAll();
        return ccc;
    }

    @Test
    public void should_test_unEscapeFieldPath() {
        Assertions.assertEquals(
                Arrays.asList(""),
                Arrays.asList(EscapeUtils.unEscapeFieldPath("")));
        Assertions.assertEquals(
                Arrays.asList("a", "a"),
                Arrays.asList(EscapeUtils.unEscapeFieldPath("a.a")));
        Assertions.assertEquals(
                Arrays.asList("a", "a.a"),
                Arrays.asList(EscapeUtils.unEscapeFieldPath("a.a&.a")));
        Assertions.assertEquals(
                Arrays.asList("a.b", "c&d"),
                Arrays.asList(EscapeUtils.unEscapeFieldPath("a&.b.c&&d")));
        Assertions.assertThrows(
                InvalidFieldExpressionException.class,
                () -> EscapeUtils.unEscapeFieldPath("&&."));
        Assertions.assertThrows(
                InvalidFieldExpressionException.class,
                () -> EscapeUtils.unEscapeFieldPath("&"));
        Assertions.assertThrows(
                InvalidFieldExpressionException.class,
                () -> EscapeUtils.unEscapeFieldPath("tom&jerry"));
    }

    @Test
    public void should_test_escapeFieldNames() {
        Assertions.assertEquals("", EscapeUtils.escapeFieldNames(new String[0]));
        Assertions.assertEquals("a.a", EscapeUtils.escapeFieldNames("a", "a"));
        Assertions.assertEquals("a&.a", EscapeUtils.escapeFieldNames("a.a"));
        Assertions.assertEquals("a&.a&&&.a", EscapeUtils.escapeFieldNames("a.a&.a"));
        Assertions.assertEquals("a&&&.b&.c&&&&d", EscapeUtils.escapeFieldNames("a&.b.c&&d"));
        Assertions.assertEquals("p.0", EscapeUtils.escapeFieldNames("p", "0"));
        Assertions.assertEquals("&&&&&.", EscapeUtils.escapeFieldNames("&&."));
        Assertions.assertEquals("&&", EscapeUtils.escapeFieldNames("&"));
        Assertions.assertEquals("tom&&jerry", EscapeUtils.escapeFieldNames("tom&jerry"));
    }

    @Test
    public void should_test_get_dotNotation() {
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
    public void should_crud_dotNotation() {
        Document doc = new Document();
        // Top Level
        doc.append("top_string", "value1");
        doc.append("top_boolean", true);
        doc.append("top_number", 1);
        doc.append("top_[", 1);

        doc.append("top_list", List.of("alpha", "beta", "gamma"));
        doc.append("top_map", Map.of("key1", "value1", "key2", "value2"));
        // Sub level with "." and "&"
        doc.append("sub.string", "string");
        doc.append("sub.boolean", true);
        doc.append("sub.number", true);
        doc.append(new String[] {"sub", "split"}, true);
        doc.append(new String[] {"sub.a", "split.b"}, true);
        doc.append("sub.list", List.of("alpha", "beta", "gamma"));
        doc.append("sub.map", Map.of("key1", "value1", "key2", "value2"));

        // Sub level with "." and "&"
        doc.append("sub2.field20&.field21.p1", List.of("alpha", "beta", "gamma"));
        doc.append("sub2.field20&.field21.p2", "hello2");
        doc.append("sub2.lvl2&&lvl3", "value2"); // Using &&

        Assertions.assertThrows(InvalidFieldExpressionException.class, () ->  doc.append("lvl1.lvl2&lvl3", "invalid"));
        System.out.println(doc.toJson());

        // Get
        Assertions.assertEquals("value1", doc.getString("top_string"));
        Assertions.assertEquals(true, doc.getBoolean("top_boolean"));
        Assertions.assertEquals(1, doc.getInteger("top_number"));
        Assertions.assertEquals(List.of("alpha", "beta", "gamma"), doc.getList("top_list", String.class));
        Assertions.assertEquals("string", doc.getString("sub.string"));
        Assertions.assertEquals("hello2", doc.getString("sub2.field20&.field21.p2"));
        Assertions.assertEquals("value2", doc.getString("sub2.lvl2&&lvl3"));
        Assertions.assertTrue(doc.containsKey("sub2.lvl2&&lvl3"));
        Assertions.assertTrue(doc.containsKey("sub2.field20&.field21.p2"));
        // get with a [] segments
        Assertions.assertEquals("value2", doc.get(new String[] {"sub2", "lvl2&lvl3"}));

        doc.remove("sub2.field20&.field21");
        Assertions.assertNull(doc.get(new String[] {"sub2", "field20.field21"}));
        System.out.println(doc.toJson());
    }

    @Test
    public void should_append_dotNotation() {
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
    }

    @Test
    public void should_test_distinct() {
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
        Assertions.assertEquals(0, result.getMatchedCount());
        Assertions.assertEquals(0, result.getModifiedCount());
        Assertions.assertNotNull(result.getUpsertedId());

        CollectionFindOneOptions options2 = new CollectionFindOneOptions()
                .sort(Sort.ascending("rating"),
                      Sort.descending("title"));
    }

    @Test
    public void should_distinct_values() {
        Database db = DataAPIClients.localDbWithDefaultKeyspace();
        Collection<Document> collection = db.createCollection("extended_names");
        collection.deleteAll();
        Document doc = new Document();
        // Top Level
        doc.append("top_string", "value1");
        doc.append("top_boolean", true);
        doc.append("top_number", 1);
        doc.append("top_[", 1);
        doc.append("top_list", List.of("alpha", "beta", "gamma"));
        doc.append("top_map", Map.of("key1", "value1", "key2", "value2"));
        // Sub level with "." and "&"
        doc.append("sub.string", "string");
        doc.append("sub.boolean", true);
        doc.append("sub.number", true);
        doc.append(new String[] {"sub", "split"}, true);
        doc.append(new String[] {"sub.a", "split.b"}, true);
        doc.append("sub.list", List.of("alpha", "beta", "gamma"));
        doc.append("sub.map", Map.of("key1", "value1", "key2", "value2"));
        // Sub level with "." and "&"
        doc.append("sub2.field20&.field21.p1", List.of("alpha", "beta", "gamma"));
        doc.append("sub2.field20&.field21.p2", "hello2");
        doc.append("sub2.lvl2&&lvl3", "value2");
        collection.insertOne(doc);

        Document doc2 = new Document();
        doc.append("sub2.field20&.field21.p2", "hello1");
        doc.append("sub.list", List.of("toto", "titi", "tata"));
        collection.insertOne(doc);

        Set<String> values = collection.distinct("sub2.field20&.field21.p2", String.class);
        Assertions.assertTrue(values.contains("hello1") && values.contains("hello2"));

        Set<String> firstItems = collection.distinct("sub.list[0]", String.class);
        Assertions.assertTrue(firstItems.contains("toto") && firstItems.contains("alpha"));
    }

}
