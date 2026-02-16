package com.datastax.astra.test.unit.collections;

import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.exceptions.InvalidFieldExpressionException;
import com.datastax.astra.internal.utils.EscapeUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class DocumentDotNotationTest {

    // --------------------------------------------------
    // EscapeUtils: unEscapeFieldPath
    // --------------------------------------------------

    @Test
    public void should_unescape_simple_path() {
        // "a.a" splits into two segments: ["a", "a"]
        assertEquals(Arrays.asList("a", "a"),
                Arrays.asList(EscapeUtils.unEscapeFieldPath("a.a")));
    }

    @Test
    public void should_unescape_empty_string() {
        // Empty string produces a single empty-string segment
        assertEquals(Arrays.asList(""),
                Arrays.asList(EscapeUtils.unEscapeFieldPath("")));
    }

    @Test
    public void should_unescape_escaped_dot() {
        // "&." is an escaped dot => literal "." stays in the segment
        // "a.a&.a" => ["a", "a.a"]
        assertEquals(Arrays.asList("a", "a.a"),
                Arrays.asList(EscapeUtils.unEscapeFieldPath("a.a&.a")));
    }

    @Test
    public void should_unescape_escaped_ampersand_and_dot() {
        // "&&" is an escaped ampersand => literal "&"
        // "a&.b.c&&d" => ["a.b", "c&d"]
        assertEquals(Arrays.asList("a.b", "c&d"),
                Arrays.asList(EscapeUtils.unEscapeFieldPath("a&.b.c&&d")));
    }

    @Test
    public void should_reject_invalid_escape_sequences() {
        // "&&." => "&&" is literal "&", then "." at end => trailing dot is invalid
        assertThrows(InvalidFieldExpressionException.class,
                () -> EscapeUtils.unEscapeFieldPath("&&."));
        // Dangling "&" at end of string
        assertThrows(InvalidFieldExpressionException.class,
                () -> EscapeUtils.unEscapeFieldPath("&"));
        // "&" followed by a regular character (not "." or "&")
        assertThrows(InvalidFieldExpressionException.class,
                () -> EscapeUtils.unEscapeFieldPath("tom&jerry"));
    }

    // --------------------------------------------------
    // EscapeUtils: escapeFieldNames
    // --------------------------------------------------

    @Test
    public void should_escape_empty_array() {
        assertEquals("", EscapeUtils.escapeFieldNames(new String[0]));
    }

    @Test
    public void should_escape_multiple_segments_with_dot() {
        // Two segments joined with "."
        assertEquals("a.a", EscapeUtils.escapeFieldNames("a", "a"));
        assertEquals("p.0", EscapeUtils.escapeFieldNames("p", "0"));
    }

    @Test
    public void should_escape_dots_in_segment() {
        // A dot inside a segment is escaped as "&."
        assertEquals("a&.a", EscapeUtils.escapeFieldNames("a.a"));
    }

    @Test
    public void should_escape_ampersands_in_segment() {
        // An ampersand inside a segment is escaped as "&&"
        assertEquals("&&", EscapeUtils.escapeFieldNames("&"));
        assertEquals("tom&&jerry", EscapeUtils.escapeFieldNames("tom&jerry"));
    }

    @Test
    public void should_escape_mixed_dots_and_ampersands() {
        assertEquals("a&.a&&&.a", EscapeUtils.escapeFieldNames("a.a&.a"));
        assertEquals("a&&&.b&.c&&&&d", EscapeUtils.escapeFieldNames("a&.b.c&&d"));
        assertEquals("&&&&&.", EscapeUtils.escapeFieldNames("&&."));
    }

    // --------------------------------------------------
    // put(): plain write — stores with the literal key
    // --------------------------------------------------

    @Test
    public void should_put_toplevel_key() {
        Document doc = new Document();
        doc.put("name", "Alice");
        assertEquals("Alice", doc.get("name"));
    }

    @Test
    public void should_put_key_with_dot_literally() {
        // put() does NOT navigate; the dot is part of the literal key
        Document doc = new Document();
        doc.put("a.b", "literal");
        assertEquals("literal", doc.get("a.b"));
        // No nested "a" map was created
        assertNull(doc.get("a"));
    }

    @Test
    public void should_put_key_with_special_chars_literally() {
        Document doc = new Document();
        doc.put("key&value", 42);
        doc.put("arr[0]", "zero");
        assertEquals(42, doc.get("key&value"));
        assertEquals("zero", doc.get("arr[0]"));
    }

    // --------------------------------------------------
    // append(String): escaping-aware write — navigates nested maps
    // --------------------------------------------------

    @Test
    public void should_append_toplevel_key() {
        // With no dots, append behaves like put
        Document doc = new Document();
        doc.append("name", "Bob");
        assertEquals("Bob", doc.get("name"));
    }

    @Test
    public void should_append_nested_with_dot() {
        // "a.b" creates nested map: { "a": { "b": "value" } }
        Document doc = new Document();
        doc.append("a.b", "value");

        assertInstanceOf(Map.class, doc.get("a"));
        assertEquals("value", doc.read("a.b"));
    }

    @Test
    public void should_append_deeply_nested() {
        // "a.b.c" creates { "a": { "b": { "c": 123 } } }
        Document doc = new Document();
        doc.append("a.b.c", 123);

        assertEquals(123, doc.read("a.b.c"));
        assertInstanceOf(Map.class, doc.read("a.b"));
    }

    @Test
    public void should_append_merge_into_existing_nested_map() {
        // Multiple appends under the same parent merge into one map
        Document doc = new Document();
        doc.append("meta.key1", "v1");
        doc.append("meta.key2", "v2");

        assertEquals("v1", doc.read("meta.key1"));
        assertEquals("v2", doc.read("meta.key2"));
    }

    @Test
    public void should_append_with_escaped_dot() {
        // "&." keeps the dot literal in the key
        // "a&.b" => single segment "a.b" (top-level key containing a dot)
        Document doc = new Document();
        doc.append("a&.b", "value");

        assertEquals("value", doc.get("a.b"));
        assertNull(doc.get("a"));
    }

    @Test
    public void should_append_with_escaped_ampersand() {
        // "&&" keeps the ampersand literal
        // "parent.child&&name" => { "parent": { "child&name": "val" } }
        Document doc = new Document();
        doc.append("parent.child&&name", "val");

        assertEquals("val", doc.read("parent.child&&name"));
    }

    @Test
    public void should_append_with_escaped_dot_in_nested_path() {
        // "sub.field&.name.value" => { "sub": { "field.name": { "value": ... } } }
        Document doc = new Document();
        doc.append("sub.field&.name.value", "deep");

        assertEquals("deep", doc.read("sub.field&.name.value"));
        // "field.name" is a single key in the "sub" map
        assertNotNull(doc.read("sub.field&.name"));
    }

    @Test
    public void should_append_reject_invalid_escape() {
        Document doc = new Document();
        // "&" followed by a regular char is invalid
        assertThrows(InvalidFieldExpressionException.class,
                () -> doc.append("a.b&c", "bad"));
    }

    @Test
    public void should_append_with_string_array() {
        // append(String[], value) uses raw segments (no escaping)
        Document doc = new Document();
        doc.append(new String[]{"level1", "level2"}, "val");
        assertEquals("val", doc.read("level1.level2"));
    }

    @Test
    public void should_append_with_string_array_containing_dots() {
        // Segment names can contain dots when passed as array
        Document doc = new Document();
        doc.append(new String[]{"a.b", "c.d"}, "val");
        // The key "a.b" is a literal map key, "c.d" is a literal key inside it
        assertEquals("val", doc.get(new String[]{"a.b", "c.d"}));
    }

    // --------------------------------------------------
    // get(String): plain read — looks up the literal key
    // --------------------------------------------------

    @Test
    public void should_get_toplevel_key() {
        Document doc = new Document().put("x", 10);
        assertEquals(10, doc.get("x"));
    }

    @Test
    public void should_get_return_null_for_missing_key() {
        Document doc = new Document();
        assertNull(doc.get("nonexistent"));
    }

    @Test
    public void should_get_not_navigate_dots() {
        // get("a.b") looks for literal key "a.b", NOT nested navigation
        Document doc = new Document();
        doc.append("a.b", "nested");
        // The literal key "a.b" does not exist — append created { "a": { "b": "nested" } }
        assertNull(doc.get("a.b"));
    }

    // --------------------------------------------------
    // get(String[]): navigates nested maps with pre-parsed segments
    // --------------------------------------------------

    @Test
    public void should_get_array_navigate_nested() {
        Document doc = new Document();
        doc.append("root.child", "hello");
        assertEquals("hello", doc.get(new String[]{"root", "child"}));
    }

    @Test
    public void should_get_array_return_null_for_missing_path() {
        Document doc = new Document();
        doc.append("root.child", "hello");
        assertNull(doc.get(new String[]{"root", "missing"}));
    }

    @Test
    public void should_get_array_with_special_chars_in_segments() {
        // Segments in array can contain dots and ampersands (they are already parsed)
        Document doc = new Document();
        doc.append(new String[]{"a.b", "c&d"}, "val");
        assertEquals("val", doc.get(new String[]{"a.b", "c&d"}));
    }

    // --------------------------------------------------
    // read(String): escaping-aware read — navigates nested maps
    // --------------------------------------------------

    @Test
    public void should_read_toplevel_key() {
        // With no dots, read behaves like get
        Document doc = new Document().put("color", "blue");
        assertEquals("blue", doc.read("color"));
    }

    @Test
    public void should_read_nested_map() {
        Document doc = new Document();
        doc.append("a.b.c", "deep");
        assertEquals("deep", doc.read("a.b.c"));
    }

    @Test
    public void should_read_return_null_for_missing_nested_path() {
        Document doc = new Document();
        doc.append("a.b", "val");
        assertNull(doc.read("a.x"));
        assertNull(doc.read("a.b.c"));
    }

    @Test
    public void should_read_array_index() {
        Document doc = new Document();
        doc.put("items", List.of("alpha", "beta", "gamma"));
        assertEquals("alpha", doc.read("items[0]"));
        assertEquals("beta", doc.read("items[1]"));
        assertEquals("gamma", doc.read("items[2]"));
    }

    @Test
    public void should_read_array_out_of_bounds_returns_null() {
        Document doc = new Document();
        doc.put("items", List.of("a", "b"));
        assertNull(doc.read("items[5]"));
        assertNull(doc.read("items[-1]"));
    }

    @Test
    public void should_read_nested_map_then_array() {
        // "data.list[1]" => navigate into "data" map, then "list" array index 1
        Document doc = new Document();
        doc.append("data.list", List.of(10, 20, 30));
        assertEquals(20, doc.read("data.list[1]"));
    }

    @Test
    public void should_read_array_then_nested_map() {
        // "items[0].name" => items array, index 0, then "name" key
        Document doc = new Document();
        doc.put("items", List.of(Map.of("name", "Alice"), Map.of("name", "Bob")));
        assertEquals("Alice", doc.read("items[0].name"));
        assertEquals("Bob", doc.read("items[1].name"));
    }

    @Test
    public void should_read_with_escaped_dot() {
        // "a&.b" reads the literal key "a.b" at top level
        Document doc = new Document();
        doc.put("a.b", "literal_dot");
        assertEquals("literal_dot", doc.read("a&.b"));
    }

    @Test
    public void should_read_with_escaped_ampersand() {
        Document doc = new Document();
        doc.append("parent.child&&name", "val");
        assertEquals("val", doc.read("parent.child&&name"));
    }

    @Test
    public void should_read_typed() {
        Document doc = new Document();
        doc.append("count.value", 42);
        assertEquals(42, doc.read("count.value", Integer.class));
    }

    @Test
    public void should_read_typed_with_conversion() {
        Document doc = new Document();
        doc.append("ratio.value", 3);
        // Integer stored, but read as Double via Jackson conversion
        assertEquals(3.0, doc.read("ratio.value", Double.class));
    }

    // --------------------------------------------------
    // readString / readInteger / readLong / readDouble / readBoolean / readList
    // --------------------------------------------------

    @Test
    public void should_readString_nested() {
        Document doc = new Document();
        doc.append("user.name", "Alice");
        assertEquals("Alice", doc.readString("user.name"));
    }

    @Test
    public void should_readString_return_null_for_missing() {
        Document doc = new Document();
        assertNull(doc.readString("missing.path"));
    }

    @Test
    public void should_readInteger_nested() {
        Document doc = new Document();
        doc.append("stats.count", 42);
        assertEquals(42, doc.readInteger("stats.count"));
    }

    @Test
    public void should_readLong_nested() {
        Document doc = new Document();
        doc.append("stats.bigcount", 100_000_000_000L);
        assertEquals(100_000_000_000L, doc.readLong("stats.bigcount"));
    }

    @Test
    public void should_readLong_from_integer_value() {
        // Integer stored, but readLong should widen it
        Document doc = new Document();
        doc.append("stats.small", 7);
        assertEquals(7L, doc.readLong("stats.small"));
    }

    @Test
    public void should_readDouble_nested() {
        Document doc = new Document();
        doc.append("metrics.ratio", 3.14);
        assertEquals(3.14, doc.readDouble("metrics.ratio"));
    }

    @Test
    public void should_readBoolean_nested() {
        Document doc = new Document();
        doc.append("flags.active", true);
        doc.append("flags.deleted", false);
        assertTrue(doc.readBoolean("flags.active"));
        assertFalse(doc.readBoolean("flags.deleted"));
    }

    @Test
    public void should_readList_nested() {
        Document doc = new Document();
        doc.append("config.tags", List.of("a", "b", "c"));
        List<String> tags = doc.readList("config.tags", String.class);
        assertEquals(List.of("a", "b", "c"), tags);
    }

    @Test
    public void should_readList_return_null_for_missing() {
        Document doc = new Document();
        assertNull(doc.readList("missing.list", String.class));
    }

    // --------------------------------------------------
    // containsKey(String): escaping-aware — delegates to read()
    // --------------------------------------------------

    @Test
    public void should_containsKey_toplevel() {
        Document doc = new Document().put("key", "val");
        assertTrue(doc.containsKey("key"));
        assertFalse(doc.containsKey("missing"));
    }

    @Test
    public void should_containsKey_nested() {
        Document doc = new Document();
        doc.append("a.b.c", "val");
        assertTrue(doc.containsKey("a.b.c"));
        assertTrue(doc.containsKey("a.b"));
        assertTrue(doc.containsKey("a"));
        assertFalse(doc.containsKey("a.x"));
    }

    @Test
    public void should_containsKey_with_escaped_path() {
        Document doc = new Document();
        doc.append("parent.child&.name", "val");
        // "parent.child&.name" => { "parent": { "child.name": "val" } }
        assertTrue(doc.containsKey("parent.child&.name"));
        assertFalse(doc.containsKey("parent.child"));
    }

    // --------------------------------------------------
    // remove(String): escaping-aware — navigates then removes
    // --------------------------------------------------

    @Test
    public void should_remove_toplevel() {
        Document doc = new Document().put("key", "val");
        doc.remove("key");
        assertFalse(doc.containsKey("key"));
    }

    @Test
    public void should_remove_nested() {
        Document doc = new Document();
        doc.append("a.b", "v1");
        doc.append("a.c", "v2");
        doc.remove("a.b");
        assertFalse(doc.containsKey("a.b"));
        // Sibling "a.c" is untouched
        assertTrue(doc.containsKey("a.c"));
    }

    @Test
    public void should_remove_deeply_nested() {
        Document doc = new Document();
        doc.append("a.b.c", "val");
        doc.remove("a.b.c");
        assertFalse(doc.containsKey("a.b.c"));
        // Parent "a.b" still exists (as an empty map)
        assertTrue(doc.containsKey("a.b"));
    }

    @Test
    public void should_remove_with_escaped_path() {
        Document doc = new Document();
        doc.append("parent.child&.name", "val");
        doc.remove("parent.child&.name");
        assertFalse(doc.containsKey("parent.child&.name"));
    }

    @Test
    public void should_remove_whole_subtree() {
        Document doc = new Document();
        doc.append("a.b.c1", "v1");
        doc.append("a.b.c2", "v2");
        doc.remove("a.b");
        assertFalse(doc.containsKey("a.b"));
        assertFalse(doc.containsKey("a.b.c1"));
    }

    // --------------------------------------------------
    // put vs append: deserialization safety
    // --------------------------------------------------

    @Test
    public void should_put_not_interpret_escape_sequences() {
        // put() treats everything literally — no escape parsing
        Document doc = new Document();
        doc.put("a&.b", "literal_escaped");
        // Stored under the literal key "a&.b"
        assertEquals("literal_escaped", doc.get("a&.b"));
        // Not stored under "a.b"
        assertNull(doc.get("a.b"));
    }

    @Test
    public void should_append_interpret_escape_sequences() {
        // append() parses escapes — "a&.b" becomes key "a.b"
        Document doc = new Document();
        doc.append("a&.b", "escaped_append");
        assertEquals("escaped_append", doc.get("a.b"));
        // The literal "a&.b" key does not exist
        assertNull(doc.get("a&.b"));
    }

    // --------------------------------------------------
    // Roundtrip: append + read symmetry
    // --------------------------------------------------

    @Test
    public void should_roundtrip_simple_nested() {
        Document doc = new Document();
        doc.append("x.y.z", "hello");
        assertEquals("hello", doc.read("x.y.z"));
    }

    @Test
    public void should_roundtrip_with_escaped_dot() {
        // Write with escaped dot, read back with same escaped path
        Document doc = new Document();
        doc.append("ns.field&.v2", "value");
        assertEquals("value", doc.read("ns.field&.v2"));
    }

    @Test
    public void should_roundtrip_with_escaped_ampersand() {
        Document doc = new Document();
        doc.append("ns.key&&val", "data");
        assertEquals("data", doc.read("ns.key&&val"));
    }

    @Test
    public void should_roundtrip_put_and_read_with_escape() {
        // put stores "a.b" literally, read("a&.b") reads literal "a.b"
        Document doc = new Document();
        doc.put("a.b", "value");
        assertEquals("value", doc.read("a&.b"));
    }

    // --------------------------------------------------
    // Edge cases
    // --------------------------------------------------

    @Test
    public void should_append_overwrite_non_map_with_nested() {
        // If a scalar is at "a", appending "a.b" replaces it with a map
        Document doc = new Document();
        doc.append("a", "scalar");
        doc.append("a.b", "nested");
        assertEquals("nested", doc.read("a.b"));
        assertInstanceOf(Map.class, doc.get("a"));
    }

    @Test
    public void should_read_return_intermediate_map() {
        Document doc = new Document();
        doc.append("a.b.c", "val");
        Object intermediate = doc.read("a.b");
        assertInstanceOf(Map.class, intermediate);
    }

    @Test
    public void should_handle_numeric_field_names() {
        // Numeric strings as field names, not array indices
        Document doc = new Document();
        doc.append("data.0.name", "first");
        assertEquals("first", doc.read("data.0.name"));
    }

    // --------------------------------------------------
    // Dot-notation array index access: "genres.0"
    // --------------------------------------------------

    @Test
    public void should_read_array_element_with_dot_notation() {
        Document doc = new Document();
        doc.put("items", List.of("alpha", "beta", "gamma"));
        assertEquals("alpha", doc.read("items.0"));
        assertEquals("beta", doc.read("items.1"));
        assertEquals("gamma", doc.read("items.2"));
    }

    @Test
    public void should_read_nested_map_in_array_with_dot_notation() {
        Document doc = new Document();
        doc.put("items", List.of(Map.of("name", "Alice"), Map.of("name", "Bob")));
        assertEquals("Alice", doc.read("items.0.name"));
        assertEquals("Bob", doc.read("items.1.name"));
    }

    @Test
    public void should_get_array_element_with_string_array_segments() {
        Document doc = new Document();
        doc.put("items", List.of("alpha", "beta", "gamma"));
        assertEquals("alpha", doc.get(new String[]{"items", "0"}));
        assertEquals("beta", doc.get(new String[]{"items", "1"}));
    }

    @Test
    public void should_return_null_for_dot_notation_out_of_bounds() {
        Document doc = new Document();
        doc.put("items", List.of("a", "b"));
        assertNull(doc.read("items.5"));
    }

    @Test
    public void should_return_null_for_non_numeric_token_on_list() {
        Document doc = new Document();
        doc.put("items", List.of("a", "b"));
        assertNull(doc.read("items.name"));
    }

    @Test
    public void should_backward_compat_numeric_map_key_still_works() {
        // When "data" is a Map, "0" should be used as a map key, not array index
        Document doc = new Document();
        doc.append("data.0.name", "first");
        assertEquals("first", doc.read("data.0.name"));
    }
}
