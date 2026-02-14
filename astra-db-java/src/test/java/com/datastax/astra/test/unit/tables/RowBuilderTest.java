package com.datastax.astra.test.unit.tables;

import com.datastax.astra.client.core.vector.DataAPIVector;
import com.datastax.astra.client.tables.definition.rows.Row;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Row typed setters and getters.
 */
class RowBuilderTest {

    // --------------------------------------------------
    // Typed setters / getters roundtrip
    // --------------------------------------------------

    @Test
    void shouldSetAndGetText() {
        Row row = new Row().addText("name", "Alice");
        assertThat(row.getText("name")).isEqualTo("Alice");
    }

    @Test
    void shouldSetAndGetAscii() {
        Row row = new Row().addAscii("key", "ascii_value");
        assertThat(row.getAscii("key")).isEqualTo("ascii_value");
    }

    @Test
    void shouldSetAndGetInt() {
        Row row = new Row().addInt("age", 42);
        assertThat(row.getInteger("age")).isEqualTo(42);
    }

    @Test
    void shouldSetAndGetBoolean() {
        Row row = new Row().addBoolean("active", true);
        assertThat(row.getBoolean("active")).isTrue();
    }

    @Test
    void shouldSetAndGetBooleanFalse() {
        Row row = new Row().addBoolean("deleted", false);
        assertThat(row.getBoolean("deleted")).isFalse();
    }

    @Test
    void shouldSetAndGetDouble() {
        Row row = new Row().addDouble("score", 3.14);
        assertThat(row.getDouble("score")).isEqualTo(3.14);
    }

    @Test
    void shouldSetAndGetFloat() {
        Row row = new Row().addFloat("ratio", 1.5f);
        assertThat(row.getFloat("ratio")).isEqualTo(1.5f);
    }

    @Test
    void shouldSetAndGetBigInt() {
        Row row = new Row().addBigInt("big", 100_000_000_000L);
        assertThat(row.getBigInt("big")).isEqualTo(100_000_000_000L);
    }

    @Test
    void shouldSetAndGetSmallInt() {
        Row row = new Row().addSmallInt("small", (short) 200);
        assertThat(row.getShort("small")).isEqualTo((short) 200);
    }

    @Test
    void shouldSetAndGetTinyInt() {
        Row row = new Row().addTinyInt("tiny", (byte) 17);
        assertThat(row.getByte("tiny")).isEqualTo((byte) 17);
    }

    @Test
    void shouldSetAndGetDecimal() {
        Row row = new Row().addDecimal("price", new BigDecimal("123.45"));
        assertThat(row.get("price", BigDecimal.class)).isEqualByComparingTo(new BigDecimal("123.45"));
    }

    @Test
    void shouldSetAndGetVarInt() {
        Row row = new Row().addVarInt("varint", new BigInteger("999999999999"));
        assertThat(row.get("varint", BigInteger.class)).isEqualTo(new BigInteger("999999999999"));
    }

    @Test
    void shouldSetAndGetTimestamp() {
        Instant now = Instant.parse("2024-06-15T10:30:00Z");
        Row row = new Row().addTimeStamp("ts", now);
        assertThat(row.getInstant("ts")).isEqualTo(now);
    }

    @Test
    void shouldSetAndGetTime() {
        LocalTime time = LocalTime.of(13, 30, 54);
        Row row = new Row().addTime("t", time);
        assertThat(row.get("t")).isEqualTo(time);
    }

    @Test
    void shouldSetAndGetUUID() {
        UUID uuid = UUID.fromString("9c5b94b1-35ad-49bb-b118-8e8fc24abf80");
        Row row = new Row().addUUID("id", uuid);
        assertThat(row.get("id")).isEqualTo(uuid);
    }

    @Test
    void shouldSetAndGetVector() {
        DataAPIVector vector = new DataAPIVector(new float[]{0.1f, 0.2f, 0.3f});
        Row row = new Row().addVector("vec", vector);
        assertThat(row.getVector("vec").getEmbeddings()).containsExactly(0.1f, 0.2f, 0.3f);
    }

    @Test
    void shouldSetAndGetVectorFromFloatArray() {
        Row row = new Row().addVector("vec", new float[]{1.0f, 2.0f});
        assertThat(row.getVector("vec").getEmbeddings()).containsExactly(1.0f, 2.0f);
    }

    // --------------------------------------------------
    // Collection types
    // --------------------------------------------------

    @Test
    void shouldSetAndGetList() {
        Row row = new Row().addList("tags", List.of("a", "b", "c"));
        assertThat(row.getList("tags", String.class)).containsExactly("a", "b", "c");
    }

    @Test
    void shouldSetAndGetSet() {
        Row row = new Row().addSet("ids", Set.of(1, 2, 3));
        assertThat(row.get("ids")).isNotNull();
    }

    @Test
    void shouldSetAndGetMap() {
        Row row = new Row().addMap("meta", Map.of("k1", "v1", "k2", "v2"));
        assertThat(row.get("meta")).isInstanceOf(Map.class);
    }

    // --------------------------------------------------
    // Null handling
    // --------------------------------------------------

    @Test
    void shouldHandleNullText() {
        Row row = new Row().addText("name", null);
        assertThat(row.getText("name")).isNull();
    }

    @Test
    void shouldHandleNullInt() {
        Row row = new Row().addInt("age", null);
        assertThat(row.getInteger("age")).isNull();
    }

    @Test
    void shouldHandleNullBoolean() {
        Row row = new Row().addBoolean("flag", null);
        assertThat(row.getBoolean("flag")).isNull();
    }

    @Test
    void shouldReturnNullForMissingKey() {
        Row row = new Row();
        assertThat(row.getText("missing")).isNull();
        assertThat(row.getInteger("missing")).isNull();
        assertThat(row.getDouble("missing")).isNull();
        assertThat(row.getBoolean("missing")).isNull();
    }

    // --------------------------------------------------
    // Row creation patterns
    // --------------------------------------------------

    @Test
    void shouldCreateRowFromStaticFactory() {
        Row row = Row.create();
        assertThat(row).isNotNull();
        assertThat(row.getColumnMap()).isEmpty();
    }

    @Test
    void shouldCreateRowFromMap() {
        Row row = new Row(Map.of("name", "Bob", "age", 30));
        assertThat(row.getText("name")).isEqualTo("Bob");
        assertThat(row.getInteger("age")).isEqualTo(30);
    }

    @Test
    void shouldParseFromJson() {
        Row row = Row.parse("{\"name\":\"Charlie\",\"age\":25}");
        assertThat(row.getText("name")).isEqualTo("Charlie");
        assertThat(row.getInteger("age")).isEqualTo(25);
    }

    @Test
    void shouldChainMultipleAdds() {
        Row row = new Row()
                .addText("name", "Test")
                .addInt("age", 10)
                .addBoolean("active", true)
                .addDouble("score", 9.5);
        assertThat(row.getText("name")).isEqualTo("Test");
        assertThat(row.getInteger("age")).isEqualTo(10);
        assertThat(row.getBoolean("active")).isTrue();
        assertThat(row.getDouble("score")).isEqualTo(9.5);
    }

    // --------------------------------------------------
    // containsKey and remove
    // --------------------------------------------------

    @Test
    void shouldCheckContainsKey() {
        Row row = new Row().addText("name", "Alice");
        assertThat(row.containsKey("name")).isTrue();
        assertThat(row.containsKey("missing")).isFalse();
    }

    @Test
    void shouldRemoveKey() {
        Row row = new Row().addText("name", "Alice").addInt("age", 30);
        row.remove("name");
        assertThat(row.containsKey("name")).isFalse();
        assertThat(row.containsKey("age")).isTrue();
    }

    @Test
    void shouldSerializeToString() {
        Row row = new Row().addText("hello", "world");
        String json = row.toString();
        assertThat(json).contains("hello");
        assertThat(json).contains("world");
    }

    // --------------------------------------------------
    // Typed get with class
    // --------------------------------------------------

    @Test
    void shouldGetWithTypedClass() {
        Row row = new Row().addInt("count", 42);
        assertThat(row.get("count", Integer.class)).isEqualTo(42);
    }

    @Test
    void shouldGetListOfIntegers() {
        Row row = new Row().addList("nums", List.of(1, 2, 3));
        List<Integer> nums = row.getList("nums", Integer.class);
        assertThat(nums).containsExactly(1, 2, 3);
    }

    @Test
    void shouldReturnNullListForMissingKey() {
        Row row = new Row();
        assertThat(row.getList("missing", String.class)).isNull();
    }

    // --------------------------------------------------
    // Vectorize and special fields
    // --------------------------------------------------

    @Test
    void shouldAddVectorize() {
        Row row = new Row().addVectorize("vector", "some text for embedding");
        assertThat(row.get("vector")).isEqualTo("some text for embedding");
    }

    @Test
    void shouldPutAll() {
        Row row = new Row();
        row.putAll(Map.of("a", 1, "b", 2));
        assertThat(row.getInteger("a")).isEqualTo(1);
        assertThat(row.getInteger("b")).isEqualTo(2);
    }
}
