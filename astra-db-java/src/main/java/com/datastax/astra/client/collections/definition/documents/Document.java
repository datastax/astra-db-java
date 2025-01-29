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

package com.datastax.astra.client.collections.definition.documents;

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

import com.datastax.astra.client.core.DataAPIKeywords;
import com.datastax.astra.client.collections.definition.documents.types.ObjectId;
import com.datastax.astra.client.core.vector.DataAPIVector;
import com.datastax.astra.internal.serdes.DataAPISerializer;
import com.datastax.astra.internal.serdes.collections.DocumentSerializer;
import com.datastax.astra.internal.utils.Utils;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.NonNull;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Represents a document without schema constraints as a Map&lt;String, Object&gt;.(key/value)
 */
public class Document implements Serializable {

    /**
     * Serializer
     */
    protected static final DataAPISerializer SERIALIZER = new DocumentSerializer();

    /**
     * Data to be used in the document.
     */
    public transient Map<String, Object> documentMap;

    /**
     * Default Constructor.
     */
    public Document() {
        documentMap = new LinkedHashMap<>();
    }

    /**
     * Getter for the document map.
     *
     * @return
     *    document map
     */
    @JsonAnyGetter
    public Map<String, Object> getDocumentMap() {
        return documentMap;
    }

    /**
     * Setter for a property in the document map
     *
     * @param key
     *    property name
     * @param value
     *    property value
     */
    @JsonAnySetter
    public void setProperty(String key, Object value) {
        append(key, value);
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
     * Create a document with no attributes.
     *
     * @return
     *      instance of document
     */
    public static Document create() {
        return new Document();
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
        return SERIALIZER.convertValue(documentMap, clazz);
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
        return new Document(SERIALIZER.unMarshallBean(json, LinkedHashMap.class));
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
    @SuppressWarnings("unchecked")
    public Document append(final String key, final Object value) {
        if (!Utils.hasLength(key)) {
            throw new IllegalArgumentException("Field name should not be null");
        }
        String[] tokens = key.split("\\.");
        Map<String, Object> currentMap = documentMap;
        for (int i = 0; i < tokens.length - 1; i++) {
            String token = tokens[i];
            Object nested = currentMap.get(token);
            if (!(nested instanceof Map)) {
                nested = new HashMap<>();
                currentMap.put(token, nested);
            }
            // Go deeper
            currentMap = (Map<String, Object>) nested;
        }

        // Finally put the value in the last token
        currentMap.put(tokens[tokens.length - 1], value);
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
        return clazz.cast(SERIALIZER.convertValue(get(key), clazz));
    }

    /**
     * Access the document identifier
     * @param clazz
     *      can be different type
     * @return
     *      value for object
     * @param <T>
     *      type of id
     */
    public <T> T getId(@NonNull final Class<T> clazz) {
        return get(DataAPIKeywords.ID.getKeyword(), clazz);
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
        return appendIfNotNull(DataAPIKeywords.ID.getKeyword(), id);
    }

    /**
     * Add a vectorize attribute to the document.
     *
     * @param text
     *      value for the vectorize attribute
     * @return
     *      self reference
     */
    public Document vectorize(String text) {
        return appendIfNotNull(DataAPIKeywords.VECTORIZE.getKeyword(), text);
    }

    /**
     * Access attribute with vectorize name if any.
     *
     * @return
     *      value for vectorize
     */
    @JsonIgnore
    public Optional<String> getVectorize() {
        return Optional.ofNullable(get(DataAPIKeywords.VECTORIZE.getKeyword(), String.class));
    }

    /**
     * Get value for vector.
     *
     * @return
     *      vector list
     */
    @JsonIgnore
    public Optional<DataAPIVector> getVector() {
        return Optional
                .ofNullable(get(DataAPIKeywords.VECTOR.getKeyword(), float[].class))
                .map(DataAPIVector::new);
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
        return append(DataAPIKeywords.VECTOR.getKeyword(), vector);
    }

    /**
     * Set the vector value.
     *
     * @param vector
     *      vector value
     * @return
     *      self reference
     */
    public Document vector(DataAPIVector vector) {
        return append(DataAPIKeywords.VECTOR.getKeyword(), vector);
    }

    /**
     * Get value for similarity.
     *
     * @return
     *      vector list
     */
    @JsonIgnore
    public Optional<Double> getSimilarity() {
        return Optional.ofNullable(get(DataAPIKeywords.SIMILARITY.getKeyword(), Double.class));
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
    public Date ge(String k) {
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
        return SERIALIZER.marshall(documentMap);
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

    /**
     * Check if the given dot-delimited path exists as a key (final segment).
     * e.g. containsKey("foo.bar") returns true if "bar" is present in the Map
     * located at "foo".
     */
    public boolean containsKey(String key) {
        if (!Utils.hasLength(key)) {
            throw new IllegalArgumentException("Field name should not be null");
        }
        String[] tokens = key.split("\\.");
        Object current = documentMap;

        // Navigate down to the second-to-last level
        for (int i = 0; i < tokens.length - 1; i++) {
            if (!(current instanceof Map)) {
                return false;
            }
            current = ((Map<?, ?>) current).get(tokens[i]);
            if (current == null) {
                return false;
            }
        }
        // Now check if we can see the last token as a key in the last map
        if (!(current instanceof Map)) {
            return false;
        }
        return ((Map<?, ?>) current).containsKey(tokens[tokens.length - 1]);
    }

    /**
     * Retrieves the value associated with the specified key from the document.
     *
     * @param key the key whose associated value is to be returned
     * @return the value associated with the specified key, or {@code null} if the key is not found
     */
    public Object get(final String key) {
        if (!Utils.hasLength(key)) {
            throw new IllegalArgumentException("Field name should not be null");
        }
        String[] tokens = key.split("\\.");
        Object current = documentMap;
        for (String token : tokens) {
            if (!(current instanceof Map)) return null;

            Matcher matcher = Pattern.compile("([a-zA-Z0-9_-]+)(\\[(\\d+)\\])?").matcher(token);
            if (!matcher.matches()) return null;

            String fieldName = matcher.group(1);
            String indexStr = matcher.group(3);

            current = ((Map<?, ?>) current).get(fieldName);
            if (indexStr != null) {
                if (!(current instanceof List)) return null;
                List<?> list = (List<?>) current;
                int idx = Integer.parseInt(indexStr);
                if (idx < 0 || idx >= list.size()) return null;
                current = list.get(idx);
            }
        }
        return current;
    }

    /**
     * Associates the specified value with the specified key in the document.
     * If the key already has a value, the old value is replaced.
     *
     * @param key   the key with which the specified value is to be associated
     * @param value the value to be associated with the specified key
     * @return the previous value associated with the key, or {@code null} if there was no mapping for the key
     */
    public Document put(final String key, final Object value) {
        return append(key, value);
    }

    /**
     * Removes the mapping for a key from the document if it is present.
     *
     * @param key the key whose mapping is to be removed
     * @return the value that was associated with the key, or {@code null} if the key was not mapped
     */
    public Document remove(final String key) {
        String[] tokens = key.split("\\.");
        Object current = documentMap;
        for (int i = 0; i < tokens.length - 1; i++) {
            if (!(current instanceof Map)) {
                return null;
            }
            current = ((Map<?, ?>) current).get(tokens[i]);
            if (current == null) {
                return null;
            }
        }
        if (!(current instanceof Map)) {
            return null;
        }
        // Remove the final segment from the map
        ((Map<?, ?>) current).remove(tokens[tokens.length - 1]);
        return this;
    }

    /**
     * Copies all mappings from the specified map to this document.
     * Existing mappings will be replaced with mappings from the provided map.
     *
     * @param map the map containing mappings to be copied to this document
     */
    public void putAll(final Map<? extends String, ?> map) {
        if (map != null) {
            map.forEach(this::append);
        }
    }

    /**
     * Removes all mappings from this document.
     * The document will be empty after this operation.
     */
    public void clear() {
        documentMap.clear();
    }

    /**
     * Compares this document to another object for equality.
     * Two documents are considered equal if their underlying maps are equal.
     *
     * @param o the object to compare with this document
     * @return {@code true} if the specified object is equal to this document, {@code false} otherwise
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Document document = (Document) o;
        return documentMap.equals(document.documentMap);
    }

    /**
     * Returns the hash code value for this document.
     * The hash code is computed based on the underlying map.
     *
     * @return the hash code value for this document
     */
    @Override
    public int hashCode() {
        return documentMap.hashCode();
    }

}
