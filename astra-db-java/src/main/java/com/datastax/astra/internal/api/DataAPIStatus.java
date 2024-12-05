package com.datastax.astra.internal.api;

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

import com.datastax.astra.client.core.vector.DataAPIVector;
import com.datastax.astra.client.exceptions.DataAPIErrorDescriptor;
import com.datastax.astra.client.tables.definition.columns.ColumnDefinition;
import com.datastax.astra.internal.serdes.DataAPISerializer;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter @Setter
public class DataAPIStatus {

    /**
     * Default properties.
     */
    public transient Map<String, Object> payload = new HashMap<>();

    /**
     * Returned when insertMany with flag
     */
    private List<DataAPIDocumentResponse> documentResponses;

    /**
     * Sort Vector returned if flag include sortVector is set to true
     */
    private DataAPIVector sortVector;

    /**
     * Warnings returned
     */
    private List<DataAPIErrorDescriptor> warnings;

    // ----------------------
    // Tables Specifics
    // ----------------------

    /**
     * PrimaryKey Schema returned
     */
    private LinkedHashMap<String, ColumnDefinition> primaryKeySchema;

    /**
     * PrimaryKey Schema returned
     */
    private LinkedHashMap<String, ColumnDefinition> projectionSchema;

    /**
     * Inserted ids.
     */
    private List<Object> insertedIds;

    /**
     * Serializer.
     */
    @JsonIgnore
    private DataAPISerializer serializer;

    /**
     * Access the insertedIds mapping.
     *
     * @param clazz
     *      current
     * @return
     *      list of inserteds ids
     * @param <T>
     *     list of class
     */
    public <T> List<T> getInsertedIds(Class<T> clazz) {
        return insertedIds.stream().map(id -> serializer.convertValue(id, clazz)).toList();
    }

    @JsonAnyGetter
    public Map<String, Object> getPayload() {
        return payload;
    }

    @JsonAnySetter
    public void setProperty(String key, Object value) {
        payload.put(key, value);
    }

    public boolean containsKey(final Object key) {
        return payload.containsKey(key);
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
        return clazz.cast(serializer.convertValue(payload.get(key), clazz));
    }

    public Object get(final Object key) {
        return payload.get(key);
    }

}
