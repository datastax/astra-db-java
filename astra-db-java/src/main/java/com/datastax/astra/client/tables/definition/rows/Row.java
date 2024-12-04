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

    @JsonAnyGetter
    public Map<String, Object> getColumnMap() {
        return columnMap;
    }

    @JsonAnySetter
    public void setProperty(String key, Object value) {
        columnMap.put(key, value);
    }

    /**
     * Create a document with no attributes.
     *
     * @return
     *      instance of document
     */
    public static Row create() {
        return new Row();
    }

    /**
     * Marshall as a document if needed.
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
    public Row addText(final String key, final String value) {
        return add(key, value);
    }
    public Row addAscii(final String key, final String value) {
        return addText(key, value);
    }
    public Row addVectorize(final String key, final String value) {
        return add(key, value);
    }

    // Integers

    public Row addVarInt(final String key, final BigInteger value) {
        return add(key, value);
    }
    public Row addBigInt(final String key, final Long value) {
        return add(key, value);
    }
    public Row addInt(final String key, final Integer value) {
        return add(key, value);
    }
    public Row addSmallInt(final String key, final Short value) {
        return add(key, value);
    }
    public Row addTinyInt(final String key, final Byte value) {
        return add(key, value);
    }
    public Row addBoolean(final String key, final Boolean value) {
        return add(key, value);
    }
    public Row addBlob(final String key, final byte[] value) {
        return add(key, value);
    }
    public Row addFloat(final String key, final Float value) {
        return add(key, value);
    }
    public Row addDecimal(String pDecimal, BigDecimal big) {
        return add(pDecimal, big);
    }
    public Row addDouble(final String key, final Double value) {
        return add(key, value);
    }
    public Row addDate(final String key, final Date value) {
        return add(key, value);
    }
    public Row addDate(final String key, final LocalDate localDate) {
        if (localDate == null) {
            return add(key, null);
        }
        return addDate(key, Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }
    public Row addInet(final String key, final InetAddress value) {
        return add(key, value);
    }
    public Row addDuration(final String key, final Duration value) {
        return add(key, TableDuration.of(Period.ZERO, value));
    }
    public Row addDuration(final String key, final Period period, final Duration duration) {
        return add(key, TableDuration.of(period, duration));
    }
    public Row addDuration(final String key, final Period period) {
        return add(key, TableDuration.of(period, Duration.ZERO));
    }
    public Row addTableDuration(final String key, final TableDuration value) {
        return add(key, value);
    }
    public Row addUUID(final String key, final UUID value) {
        return add(key, value);
    }
    public Row addTimeStamp(String key, Instant instant) {
        return add(key, instant);
    }
    public Row addTime(String key, LocalTime ltime) {
        return add(key, ltime);
    }
    public Row addVector(String key, DataAPIVector vector) {
        return add(key, vector);
    }
    public Row addVector(String key, float[] vector) {
        return add(key, new DataAPIVector(vector));
    }
    public <T> Row addList(String key, List<T> list) {
        return add(key, list);
    }
    public <T> Row addSet(String key, Set<T> set) {
        return add(key, set);
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

