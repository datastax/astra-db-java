package com.datastax.astra.client.model.tables;

import com.datastax.astra.client.model.collections.Document;
import com.datastax.astra.client.model.types.DataAPIKeywords;
import com.datastax.astra.client.model.types.ObjectId;
import com.datastax.astra.internal.utils.JsonUtils;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.NonNull;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.time.Instant;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Record present in a Cassandra Table.
 */
public class Row implements Map<String, Object>, Serializable {

    /**
     * Data to be used in the document.
     */
    @JsonUnwrapped
    public transient Map<String, Object> columnMap;

    /**
     * Default Constructor.
     */
    public Row() {
        columnMap = new LinkedHashMap<>();
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
        return JsonUtils.convertValue(columnMap, clazz);
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
        return new Row(JsonUtils.unMarshallBean(json, LinkedHashMap.class));
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
    public Row append(final String key, final Object value) {
        columnMap.put(key, value);
        return this;
    }

    /**
     * Put the given key/value pair into this Document and return this only if the value is not null
     * <pre>
     * doc.append("a", 1).append("b", 2)}
     * </pre>
     * @param key   key
     * @param value value
     * @return this
     */
    public Row appendIfNotNull(final String key, final Object value) {
        if (value != null) {
            return append(key, value);
        }
        return this;
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
        return clazz.cast(JsonUtils.convertValue(columnMap.get(key), clazz));
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
     * Gets the value of the given key as a Long.
     *
     * @param key the key
     * @return the value as a long, which may be null
     * @throws ClassCastException if the value is not a long
     */
    public Long getLong(final String key) {
        Object o = get(key);
        if (o instanceof Integer) {
            return ((Integer) o).longValue();
        }
        return (Long) get(key);
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
    public String getString(final String key) {
        return (String) get(key);
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
    public ObjectId getObjectId(String k) {
        Object o = get(k);
        if (o == null) {
            return null;
        }
        if (!(o instanceof LinkedHashMap)) {
            throw new IllegalArgumentException("UUID must be a string or a map with a $objectId key but found " + o);
        }
        LinkedHashMap<String, String> map = (LinkedHashMap<String, String>) o;
        if (!map.containsKey(DataAPIKeywords.OBJECT_ID.getKeyword())) {
            throw new IllegalArgumentException("UUID must be a string or a map with a $objectId key but found " + o);
        }
        return  new ObjectId(map.get(DataAPIKeywords.OBJECT_ID.getKeyword()));
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
     * @since 3.10
     */
    public <T> List<T> getList(@NonNull final String key, @NonNull final Class<T> clazz) {
        return constructValuesList(key, clazz, null);
    }

    /**
     * Gets the list value of the given key, casting the list elements to {@code Class<T>} or returning the default list value if null.
     * This is useful to avoid having casts in client code, though the effect is the same.
     *
     * @param key   the key
     * @param clazz the non-null class to cast the list value to
     * @param defaultValue what to return if the value is null
     * @param <T>   the type of the class
     * @return the list value of the given key, or the default list value if the instance does not contain this key.
     * @throws ClassCastException if the value of the given key is not of type T
     * @since 3.10
     */
    public <T> List<T> getList(final String key, final Class<T> clazz, final List<T> defaultValue) {
        return constructValuesList(key, clazz, defaultValue);
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> constructValuesList(final String key, final Class<T> clazz, final List<T> defaultValue) {
        List<T> value = get(key, List.class);
        if (value == null) {
            return defaultValue;
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
        return JsonUtils.marshall(columnMap);
    }

    /**
     * Serialization with Jackson.
     *
     * @return
     *      json string
     */
    public String toJson() {
        return toString();
    }

    // Vanilla Map methods delegate to map field

    /** {@inheritDoc} */
    @Override
    public int size() {
        return columnMap.size();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
        return columnMap.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsValue(final Object value) {
        return columnMap.containsValue(value);
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsKey(final Object key) {
        return columnMap.containsKey(key);
    }

    /** {@inheritDoc} */
    @Override
    public Object get(final Object key) {
        return columnMap.get(key);
    }

    /** {@inheritDoc} */
    @Override
    public Object put(final String key, final Object value) {
        return columnMap.put(key, value);
    }

    /** {@inheritDoc} */
    @Override
    public Object remove(final Object key) {
        return columnMap.remove(key);
    }

    /** {@inheritDoc} */
    @Override
    public void putAll(final Map<? extends String, ?> map) {
        columnMap.putAll(map);
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {
        columnMap.clear();
    }

    /** {@inheritDoc} */
    @Override
    public Set<String> keySet() {
        return columnMap.keySet();
    }

    /** {@inheritDoc} */
    @Override
    public Collection<Object> values() {
        return columnMap.values();
    }

    /** {@inheritDoc} */
    @Override
    public Set<Entry<String, Object>> entrySet() {
        return columnMap.entrySet();
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

