package com.datastax.astra.client.tables.definition.rows;

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

import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.DataAPIKeywords;
import com.datastax.astra.client.core.vector.DataAPIVector;
import com.datastax.astra.client.tables.definition.TableDuration;
import com.datastax.astra.internal.serdes.DataAPISerializer;
import com.datastax.astra.internal.serdes.tables.RowSerializer;
import com.datastax.astra.internal.utils.Assert;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.NonNull;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.datastax.astra.internal.utils.Assert.hasLength;

/**
 * Record present in a Cassandra Table.
 */
public class Row implements Serializable {

    /** Serializer for the Rows. */
    protected static final DataAPISerializer SERIALIZER = new RowSerializer();

    /** Data to be used in the document. */
    public transient Map<String, Object> columnMap;

    /**
     * Default Constructor.
     */
    public Row() {
        columnMap = new LinkedHashMap<>();
    }

    /**
     * Access internal property map.
     *
     * @return
     *     internal map
     */
    @JsonAnyGetter
    public Map<String, Object> getColumnMap() {
        return columnMap;
    }

    /**
     * Set a property in the row.
     *
     * @param key
     *      key
     * @param value
     *      value
     */
    @JsonAnySetter
    public void setProperty(String key, Object value) {
        columnMap.put(key, value);
    }

    /**
     * Create a row with no attributes.
     *
     * @return
     *      instance of document
     */
    public static Row create() {
        return new Row();
    }

    /**
     * Marshall as a row if needed.
     *
     * @param clazz
     *      target class
     * @return
     *      instance of pojo
     * @param <T>
     *      current type
     */
    public <T> T map(Class<T> clazz) {
        return SERIALIZER.convertValue(columnMap, clazz);
    }

    /**
     * Creates a Document instance initialized with the given map.
     *
     * @param map initial map
     */
    public Row(final Map<String, ?> map) {
        columnMap = new LinkedHashMap<>(map);
    }

    /**
     * Parses a string in MongoDB Extended JSON format to a {@code Document}
     *
     * @param json
     *      the JSON string
     * @return
     *      the document
     */
    @SuppressWarnings("unchecked")
    public static Row parse(final String json) {
        return new Row(SERIALIZER.unMarshallBean(json, LinkedHashMap.class));
    }

    /**
     * Put the given key/value pair into this Document and return this.  Useful for chaining puts in a single expression, e.g.
     * <pre>
     * doc.append("a", 1).append("b", 2)}
     * </pre>
     * @param key   key
     * @param value value
     * @return this
     */
    public Row add(final String key, final Object value) {
        hasLength(key, "Key must not be null or empty");
        columnMap.put(key, value);
        return this;
    }

    /**
     * Adds a text value to the row.
     *
     * @param key   the key for the text value
     * @param value the text value to add
     * @return the updated row
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * row.addText("name", "example");
     * }
     * </pre>
     */
    public Row addText(final String key, final String value) {
        return add(key, value);
    }

    /**
     * Adds an ASCII-encoded text value to the row.
     *
     * @param key   the key for the ASCII value
     * @param value the ASCII value to add
     * @return the updated row
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * row.addAscii("key", "ASCII value");
     * }
     * </pre>
     */
    public Row addAscii(final String key, final String value) {
        return addText(key, value);
    }

// Repeat the following template for each method, adapting parameters and descriptions accordingly

    /**
     * Adds a vectorized value to the row.
     *
     * @param key   the key for the vectorized value
     * @param value the vectorized value to add
     * @return the updated row
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * row.addVectorize("vectorKey", "vectorizedData");
     * }
     * </pre>
     */
    public Row addVectorize(final String key, final String value) {
        return add(key, value);
    }

    /**
     * Adds a variable-length integer (VarInt) value to the row.
     *
     * @param key   the key for the VarInt value
     * @param value the VarInt value to add
     * @return the updated row
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * row.addVarInt("key", new BigInteger("123456789"));
     * }
     * </pre>
     */
    public Row addVarInt(final String key, final BigInteger value) {
        return add(key, value);
    }

    /**
     * Adds a BigInt value to the row.
     *
     * @param key   the key for the BigInt value
     * @param value the BigInt value to add
     * @return the updated row
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * row.addBigInt("key", 123456789L);
     * }
     * </pre>
     */
    public Row addBigInt(final String key, final Long value) {
        return add(key, value);
    }

    /**
     * Adds an Integer value to the row.
     *
     * @param key   the key for the Integer value
     * @param value the Integer value to add
     * @return the updated row
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * row.addInt("key", 42);
     * }
     * </pre>
     */
    public Row addInt(final String key, final Integer value) {
        return add(key, value);
    }

    /**
     * Adds a SmallInt value to the row.
     *
     * @param key   the key for the SmallInt value
     * @param value the SmallInt value to add
     * @return the updated row
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * row.addSmallInt("key", (short) 32000);
     * }
     * </pre>
     */
    public Row addSmallInt(final String key, final Short value) {
        return add(key, value);
    }

    /**
     * Adds a TinyInt value to the row.
     *
     * @param key   the key for the TinyInt value
     * @param value the TinyInt value to add
     * @return the updated row
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * row.addTinyInt("key", (byte) 127);
     * }
     * </pre>
     */
    public Row addTinyInt(final String key, final Byte value) {
        return add(key, value);
    }
    /**
     * Adds a Boolean value to the row.
     *
     * @param key   the key for the Boolean value
     * @param value the Boolean value to add
     * @return the updated row
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * row.addBoolean("key", true);
     * }
     * </pre>
     */
    public Row addBoolean(final String key, final Boolean value) {
        return add(key, value);
    }

    /**
     * Adds a Blob (byte array) to the row.
     *
     * @param key   the key for the Blob value
     * @param value the byte array to add
     * @return the updated row
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * byte[] data = {1, 2, 3};
     * row.addBlob("key", data);
     * }
     * </pre>
     */
    public Row addBlob(final String key, final byte[] value) {
        return add(key, value);
    }

    /**
     * Adds a Float value to the row.
     *
     * @param key   the key for the Float value
     * @param value the Float value to add
     * @return the updated row
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * row.addFloat("key", 3.14f);
     * }
     * </pre>
     */
    public Row addFloat(final String key, final Float value) {
        return add(key, value);
    }

    /**
     * Adds a Decimal (BigDecimal) value to the row.
     *
     * @param key   the key for the Decimal value
     * @param value the BigDecimal value to add
     * @return the updated row
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * row.addDecimal("key", new BigDecimal("12345.6789"));
     * }
     * </pre>
     */
    public Row addDecimal(String key, BigDecimal value) {
        return add(key, value);
    }

    /**
     * Adds a Double value to the row.
     *
     * @param key   the key for the Double value
     * @param value the Double value to add
     * @return the updated row
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * row.addDouble("key", 3.14159);
     * }
     * </pre>
     */
    public Row addDouble(final String key, final Double value) {
        return add(key, value);
    }

    /**
     * Adds a Date value to the row.
     *
     * @param key   the key for the Date value
     * @param value the Date value to add
     * @return the updated row
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * row.addDate("key", new Date());
     * }
     * </pre>
     */
    public Row addDate(final String key, final Date value) {
        return add(key, value);
    }


    /**
     * Adds a LocalDate value to the row, converting it to a Date.
     *
     * @param key       the key for the LocalDate value
     * @param localDate the LocalDate value to add
     * @return the updated row
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * row.addDate("key", LocalDate.now());
     * }
     * </pre>
     */
    public Row addLocalDate(final String key, final LocalDate localDate) {
        if (localDate == null) {
            return add(key, null);
        }
        return addDate(key, Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    /**
     * Adds an InetAddress value to the row.
     *
     * @param key   the key for the InetAddress value
     * @param value the InetAddress value to add
     * @return the updated row
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * row.addInet("key", InetAddress.getByName("127.0.0.1"));
     * }
     * </pre>
     */
    public Row addInet(final String key, final InetAddress value) {
        return add(key, value);
    }

    /**
     * Adds a Duration value to the row.
     *
     * @param key      the key for the Duration value
     * @param duration the Duration value to add
     * @return the updated row
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * row.addDuration("key", Duration.ofMinutes(10));
     * }
     * </pre>
     */
    public Row addDuration(final String key, final Duration duration) {
        return add(key, TableDuration.of(Period.ZERO, duration));
    }

    /**
     * Adds a Duration value to the row.
     *
     * @param key    the key for the Duration value
     * @param period the Period value to add
     * @param duration the Duration value to add
     * @return the updated row
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * row.addDuration("key", Period.ofDays(1), Duration.ofMinutes(10));
     * }
     * </pre>
     */
    public Row addDuration(final String key, final Period period, final Duration duration) {
        return add(key, TableDuration.of(period, duration));
    }

    /**
     * Adds a duration value composed of only a Period to the row.
     *
     * @param key    the key for the duration value
     * @param period the period component of the duration
     * @return the updated row
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * row.addDuration("key", Period.ofWeeks(2));
     * }
     * </pre>
     */
    public Row addDuration(final String key, final Period period) {
        return add(key, TableDuration.of(period, Duration.ZERO));
    }

    /**
     * Adds a TableDuration value to the row.
     *
     * @param key   the key for the TableDuration value
     * @param value the TableDuration value to add
     * @return the updated row
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * row.addTableDuration("key", new TableDuration(Period.ofMonths(1), Duration.ofMinutes(30)));
     * }
     * </pre>
     */
    public Row addTableDuration(final String key, final TableDuration value) {
        return add(key, value);
    }


    /**
     * Adds a UUID value to the row.
     *
     * @param key   the key for the UUID value
     * @param value the UUID value to add
     * @return the updated row
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * row.addUUID("key", UUID.randomUUID());
     * }
     * </pre>
     */
    public Row addUUID(final String key, final UUID value) {
        return add(key, value);
    }

    /**
     * Adds a timestamp (Instant) value to the row.
     *
     * @param key    the key for the timestamp
     * @param instant the Instant value to add
     * @return the updated row
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * row.addTimeStamp("key", Instant.now());
     * }
     * </pre>
     */
    public Row addTimeStamp(String key, Instant instant) {
        return add(key, instant);
    }

    /**
     * Adds a LocalTime value to the row.
     *
     * @param key   the key for the time value
     * @param ltime the LocalTime value to add
     * @return the updated row
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * row.addTime("key", LocalTime.now());
     * }
     * </pre>
     */
    public Row addTime(String key, LocalTime ltime) {
        return add(key, ltime);
    }

    /**
     * Adds a DataAPIVector value to the row.
     *
     * @param key    the key for the vector
     * @param vector the DataAPIVector to add
     * @return the updated row
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * DataAPIVector vector = new DataAPIVector(new float[]{1.0f, 2.0f, 3.0f});
     * row.addVector("key", vector);
     * }
     * </pre>
     */
    public Row addVector(String key, DataAPIVector vector) {
        return add(key, vector);
    }

    /**
     * Adds a vector (float array) to the row, wrapping it in a DataAPIVector.
     *
     * @param key    the key for the vector
     * @param vector the float array to add
     * @return the updated row
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * row.addVector("key", new float[]{1.0f, 2.0f, 3.0f});
     * }
     * </pre>
     */
    public Row addVector(String key, float[] vector) {
        return add(key, new DataAPIVector(vector));
    }

    /**
     * Adds a list to the row.
     *
     * @param key  the key for the list
     * @param list the list to add
     * @param <T>  the type of elements in the list
     * @return the updated row
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * List<String> names = List.of("Alice", "Bob");
     * row.addList("key", names);
     * }
     * </pre>
     */
    public <T> Row addList(String key, List<T> list) {
        return add(key, list);
    }

    /**
     * Adds a set to the row.
     *
     * @param key the key for the set
     * @param set the set to add
     * @param <T> the type of elements in the set
     * @return the updated row
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Set<Integer> numbers = Set.of(1, 2, 3);
     * row.addSet("key", numbers);
     * }
     * </pre>
     */
    public <T> Row addSet(String key, Set<T> set) {
        return add(key, set);
    }


    /**
     * Adds a map to the row.
     *
     * @param key   the key for the map
     * @param myMap the map to add
     * @param <K>   the type of keys in the map
     * @param <V>   the type of values in the map
     * @return the updated row
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Map<String, Integer> exampleMap = Map.of("one", 1, "two", 2);
     * row.addMap("key", exampleMap);
     * }
     * </pre>
     */
    public <K, V> Row addMap(String key, Map<K, V> myMap) {
        return add(key, myMap);
    }

    /**
     * Gets the value of the given key, casting it to the given {@code Class<T>}.  This is useful to avoid having casts in client code,
     * though the effect is the same.  So to get the value of a key that is of type String, you would write {@code String name =
     * doc.get("name", String.class)} instead of {@code String name = (String) doc.get("x") }.
     *
     * @param key   the key
     * @param clazz the non-null class to cast the value to
     * @param <T>   the type of the class
     * @return the value of the given key, or null if the instance does not contain this key.
     * @throws ClassCastException if the value of the given key is not of type T
     */
    public <T> T get(@NonNull final String key, @NonNull final Class<T> clazz) {
        Assert.hasLength(key, "key");
        return clazz.cast(SERIALIZER.convertValue(columnMap.get(key), clazz));
    }

    /**
     * Gets the value of the given key as an Integer.
     *
     * @param key the key
     * @return the value as an integer, which may be null
     * @throws ClassCastException if the value is not an integer
     */
    public Integer getInteger(final String key) {
        return (Integer) get(key);
    }

    /**
     * Gets the value of the given key as a Double.
     *
     * @param key the key
     * @return the value as a double, which may be null
     * @throws ClassCastException if the value is not a double
     */
    public Double getDouble(final String key) {
        return (Double) get(key);
    }

    /**
     * Gets the value of the given key as a String.
     *
     * @param key the key
     * @return the value as a String, which may be null
     * @throws ClassCastException if the value is not a String
     */
    public String getAscii(final String key) {
        return (String) get(key);
    }
    public String getText(final String key) {
        return (String) get(key);
    }

    public Long getBigInt(final String key) {
        return Long.parseLong(String.valueOf(get(key)));
    }

    public DataAPIVector getVector(final String key) {
        return get(key, DataAPIVector.class);
    }

    /**
     * Gets the value of the given key as a Boolean.
     *
     * @param key the key
     * @return the value as a Boolean, which may be null
     * @throws ClassCastException if the value is not a boolean
     */
    public Boolean getBoolean(final String key) {
        return (Boolean) get(key);
    }

    /**
     * Gets the value of the given key as a Date.
     *
     * @param key the key
     * @return the value as a Date, which may be null
     * @throws ClassCastException if the value is not a Date
     */
    public Date getDate(final Object key) {
        return (Date) get(key);
    }

    /**
     * Return an Array of items.
     *
     * @param k
     *      key
     * @param itemClass
     *      expected class
     * @return
     *      list of items
     * @param <K>
     *      type of item
     */
    @SuppressWarnings("unchecked")
    public <K> K[] getArray(String k, Class<K> itemClass) {
        List<K> list = getList(k, itemClass);
        K[] array = (K[]) Array.newInstance(itemClass, list.size());
        return list.toArray(array);
    }

    /**
     * Access element from the map
     * @param k
     *      current configuration key
     * @return
     *      configuration value
     */
    @SuppressWarnings("unchecked")
    public UUID getUUID(String k) {
        Object o = get(k);
        if (o == null) {
            return null;
        }
        if (!(o instanceof LinkedHashMap)) {
            throw new IllegalArgumentException("UUID must be a string or a map with a $uuid key but found " + o);
        }
        LinkedHashMap<String, String> map = (LinkedHashMap<String, String>) o;
        if (!map.containsKey(DataAPIKeywords.UUID.getKeyword())) {
            throw new IllegalArgumentException("UUID must be a string or a map with a $uuid key but found " + o);
        }
        return  UUID.fromString(map.get(DataAPIKeywords.UUID.getKeyword()));
    }

    /**
     * Access element from the map
     * @param k
     *      current configuration key
     * @return
     *      configuration value
     */
    public Float getFloat(String k) {
        return get(k, Float.class);
    }

    /**
     * Access element from the map
     * @param k
     *      current configuration key
     * @return
     *      configuration value
     */
    public Short getShort(String k) {
        return get(k, Short.class);
    }

    /**
     * Access element from the map
     * @param k
     *      current configuration key
     * @return
     *      configuration value
     */
    public Byte getByte(String k) {
        return get(k, Byte.class);
    }

    /**
     * Access element from the map
     * @param k
     *      current configuration key
     * @return
     *      configuration value
     */
    public Character getCharacter(String k) {
        return  get(k, Character.class);
    }

    /**
     * Access element from the map
     * @param k
     *      current configuration key
     * @return
     *      configuration value
     */
    public Date getDate(String k) {
        return get(k, Date.class);
    }

    /**
     * Access element from the map
     * @param k
     *      current configuration key
     * @return
     *      configuration value
     */
    public Calendar getCalendar(String k) {
        return get(k, Calendar.class);
    }

    /**
     * Access element from the map
     * @param k
     *      current configuration key
     * @return
     *      configuration value
     */
    public Instant getInstant(String k) {
        return get(k, Instant.class);
    }

    /**
     * Gets the list value of the given key, casting the list elements to the given {@code Class<T>}.  This is useful to avoid having
     * casts in client code, though the effect is the same.
     *
     * @param key   the key
     * @param clazz the non-null class to cast the list value to
     * @param <T>   the type of the class
     * @return the list value of the given key, or null if the instance does not contain this key.
     * @throws ClassCastException if the elements in the list value of the given key is not of type T or the value is not a list
     */
    public <T> List<T> getList(@NonNull final String key, @NonNull final Class<T> clazz) {
        List<T> value = get(key, List.class);
        if (value == null) {
            return null;
        }
        for (Object item : value) {
            if (item != null && !clazz.isAssignableFrom(item.getClass())) {
                throw new ClassCastException(String.format("List element cannot be cast to %s", clazz.getName()));
            }
        }
        return value;
    }

    /**
     * Serialization with Jackson.
     *
     * @return
     *      json string
     */
    @Override
    public String toString() {
        return SERIALIZER.marshall(columnMap);
    }

    /** {@inheritDoc} */
    public boolean containsKey(final Object key) {
        return columnMap.containsKey(key);
    }

    /** {@inheritDoc} */
    public Object get(final Object key) {
        return columnMap.get(key);
    }

    /** {@inheritDoc} */
    public Object put(final String key, final Object value) {
        return columnMap.put(key, value);
    }

    /** {@inheritDoc} */
    public void putAll(final Map<? extends String, ?> map) {
        columnMap.putAll(map);
    }

    public void putAll(Row row) {
        if (row !=null) {
            columnMap.putAll(row.getColumnMap());
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Document document = (Document) o;
        return columnMap.equals(document.documentMap);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return columnMap.hashCode();
    }

}

