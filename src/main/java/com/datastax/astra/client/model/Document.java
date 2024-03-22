/*
 * Copyright DataStax, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.datastax.astra.client.model;

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

import com.datastax.astra.client.internal.utils.JsonUtils;
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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;


/**
 * Represents a document without schema contraints as a Map&lt;String, Object&gt;.(key/value)
 */
public class Document implements Map<String, Object>, Serializable {

    /**
     * Attribute for id.
     */
    public static final String ID = "_id";

    /**
     * Attribute for vector.
     */
    public static final String VECTOR = "$vector";

    /**
     * Attribute for vector.
     */
    public static final String VECTORIZE = "vectorize";

    /**
     * Attribute for similarity
     */
    public static final String SIMILARITY = "$similarity";

    /**
     * Data to be used in the document.
     */
    @JsonUnwrapped
    public LinkedHashMap<String, Object> documentMap;

    /**
     * Default Constructor.
     */
    public Document() {
        documentMap = new LinkedHashMap<>();
    }

    /**
     * Default Constructor.
     *
     * @param id
     *      provide the unique identifier.
     */
    public Document(Object id) {
        this();
        id(id);
    }

    /**
     * Create a document with an id.
     *
     * @param id
     *      document identifier
     * @return
     *      instance of document
     */
    public static Document create(Object id) {
        return new Document(id);
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
        return JsonUtils.convertValueForDataApi(documentMap, clazz);
    }

    /**
     * Create a Document instance initialized with the given key/value pair.
     *
     * @param key   key
     * @param value value
     */
    public Document(final String key, final Object value) {
        documentMap = new LinkedHashMap<>();
        documentMap.put(key, value);
    }

    /**
     * Creates a Document instance initialized with the given map.
     *
     * @param map initial map
     */
    public Document(final Map<String, ?> map) {
        documentMap = new LinkedHashMap<>(map);
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
    public static Document parse(final String json) {
        return new Document(JsonUtils.unmarshallBeanForDataApi(json, LinkedHashMap.class));
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
    public Document append(final String key, final Object value) {
        documentMap.put(key, value);
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
    public Document appendIfNotNull(final String key, final Object value) {
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
        return clazz.cast(JsonUtils.convertValueForDataApi(documentMap.get(key), clazz));
    }

    /**
     * Gets the value of the given key, casting it to {@code Class<T>} or returning the default value if null.
     * This is useful to avoid having casts in client code, though the effect is the same.
     *
     * @param key   the key
     * @param defaultValue what to return if the value is null
     * @param <T>   the type of the class
     * @return the value of the given key, or null if the instance does not contain this key.
     * @throws ClassCastException if the value of the given key is not of type T
     */
    @SuppressWarnings("unchecked")
    public <T> T get(@NonNull final String key, @NonNull  final T defaultValue) {
        Object value = JsonUtils.convertValueForDataApi(documentMap.get(key), defaultValue.getClass());
        return value == null ? defaultValue : (T) value;
    }

    /**
     * Access the document identiifir
     * @param clazz
     *      can be different type
     * @return
     *      value for object
     * @param <T>
     *      type of id
     */
    public <T> T getId(@NonNull final Class<T> clazz) {
        return get(ID, clazz);
    }

    /**
     * Set value for the identifier.
     *
     * @param id
     *      id value
     * @param <T>
     *      type of id
     * @return
     *      self reference
     */
    public <T> Document id(T id) {
        return appendIfNotNull(ID, id);
    }

    /**
     * Add a vectorize attribute to the document.
     *
     * @param text
     *      value for the vctorize attribute
     * @return
     *      self reference
     */
    public Document vectorize(String text) {
        return appendIfNotNull(VECTORIZE, text);
    }

    /**
     * Access attribute with vectorize name if any.
     *
     * @return
     *      value for vectorize
     */
    public Optional<String> getVectorize() {
        return Optional.ofNullable(get(VECTORIZE, String.class));
    }

    /**
     * Get value for vector.
     *
     * @return
     *      vector list
     */
    public Optional<float[]>  getVector() {
        return Optional.ofNullable(get(VECTOR, float[].class));
    }

    /**
     * Set the vector value.
     *
     * @param vector
     *      vector value
     * @return
     *      self reference
     */
    public Document vector(float[] vector) {
        return append(VECTOR, vector);
    }

    /**
     * Get value for similarity.
     *
     * @return
     *      vector list
     */
    public Optional<Double> getSimilarity() {
        return Optional.ofNullable(get(SIMILARITY, Double.class));
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
     * Gets the value of the given key as a primitive int.
     *
     * @param key          the key
     * @param defaultValue what to return if the value is null
     * @return the value as an integer, which may be null
     * @throws ClassCastException if the value is not an integer
     */
    public int getInteger(final String key, final int defaultValue) {
        return get(key, defaultValue);
    }

    /**
     * Gets the value of the given key as a Long.
     *
     * @param key the key
     * @return the value as a long, which may be null
     * @throws ClassCastException if the value is not an long
     */
    public Long getLong(final String key) {
        return (Long) get(key);
    }

    /**
     * Gets the value of the given key as a Double.
     *
     * @param key the key
     * @return the value as a double, which may be null
     * @throws ClassCastException if the value is not an double
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
     * @throws ClassCastException if the value is not an boolean
     */
    public Boolean getBoolean(final String key) {
        return (Boolean) get(key);
    }

    /**
     * Gets the value of the given key as a primitive boolean.
     *
     * @param key          the key
     * @param defaultValue what to return if the value is null
     * @return the value as a primitive boolean
     * @throws ClassCastException if the value is not a boolean
     */
    public boolean getBoolean(final String key, final boolean defaultValue) {
        return get(key, defaultValue);
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
    public UUID getUUID(String k) {
        String uuid = getString(k);
        return (uuid == null) ? null : UUID.fromString(uuid);
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
        return JsonUtils.marshallForDataApi(documentMap);
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
        return documentMap.size();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
        return documentMap.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsValue(final Object value) {
        return documentMap.containsValue(value);
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsKey(final Object key) {
        return documentMap.containsKey(key);
    }

    /** {@inheritDoc} */
    @Override
    public Object get(final Object key) {
        return documentMap.get(key);
    }

    /** {@inheritDoc} */
    @Override
    public Object put(final String key, final Object value) {
        return documentMap.put(key, value);
    }

    /** {@inheritDoc} */
    @Override
    public Object remove(final Object key) {
        return documentMap.remove(key);
    }

    /** {@inheritDoc} */
    @Override
    public void putAll(final Map<? extends String, ?> map) {
        documentMap.putAll(map);
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {
        documentMap.clear();
    }

    /** {@inheritDoc} */
    @Override
    public Set<String> keySet() {
        return documentMap.keySet();
    }

    /** {@inheritDoc} */
    @Override
    public Collection<Object> values() {
        return documentMap.values();
    }

    /** {@inheritDoc} */
    @Override
    public Set<Entry<String, Object>> entrySet() {
        return documentMap.entrySet();
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

        if (!documentMap.equals(document.documentMap)) {
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return documentMap.hashCode();
    }




}
