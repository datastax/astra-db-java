package com.datastax.astra.client.collections.commands;

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

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Encore the update document
 */
public class Update extends Document {

    /**
     * Default constructor.
     */
    public Update() {
        super();
    }

    /**
     * Default constructor.
     *
     * @param json
     *      filter expression as JSON
     */
    @SuppressWarnings("unchecked")
    public Update(String json) {
        super();
        this.documentMap.putAll(SERIALIZER.unMarshallBean(json, Map.class));
    }

    /**
     * Default constructor.
     *
     * @param obj
     *      filter expression as JSON
     */
    public Update(Map<String, Object> obj) {
        super();
        this.documentMap.putAll(obj);
    }

    /**
     * Allow to creat a new instance.
     *
     * @return
     *      a new instance
     */
    public static Update create() {
        return new Update();
    }

    /**
     * Builder pattern
     *
     * @param key
     *      field name
     * @param offset
     *      increment value
     * @return
     *      reference to self
     */
    public Update inc(String key, Double offset) {
        return update("$inc", key, offset);
    }

    /**
     * Builder pattern
     *
     * @param fieldName
     *      field name
     * @return
     *      reference to self
     */
    public Update unset(String fieldName) {
        return update("$unset", fieldName, "");
    }

    /**
     * Builder pattern
     *
     * @param key
     *      field name
     * @param value
     *      filed value
     * @return
     *      reference to self
     */
    public Update set(String key, Object value) {
        return update("$set", key, value);
    }


    /**
     * Builder pattern
     *
     * @param fields`
     *     fields map
     * @return
     *      reference to self
     */
    public Update set(Document fields) {
        if (fields !=null) fields.getDocumentMap().forEach(this::set);
        return this;
    }

    /**
     * Builder pattern
     *
     * @param key
     *      field name
     * @param value
     *      filed value
     * @return
     *      reference to self
     */
    public Update min(String key, Object value) {
        return update("$min", key, value);
    }

    /**
     * Builder pattern
     *
     * @param key
     *      field name
     * @param value
     *      filed value
     * @return
     *      reference to self
     */
    public Update push(String key, Object value) {
        return update("$push", key, value);
    }

    /**
     * Builder pattern
     *
     * @param key
     *      field name
     * @param value
     *      filed value
     * @return
     *      reference to self
     */
    public Update pop(String key, Object value) {
        return update("$pop", key, value);
    }

    /**
     * Builder pattern.
     *
     * @param key
     *      field name
     * @param values
     *      filed list values
     * @param position
     *      where to push in the list
     * @return
     *      reference to self
     */
    public Update pushEach(String key, List<Object> values, Integer position) {
        // The value need "$each"
        Map<String, Object> value = new HashMap<>();
        value.put("$each", values);
        if (null != position) {
            value.put("$position", position);
        }
        return update("$push", key, value);
    }

    /**
     * Builder pattern
     *
     * @param key
     *      field name
     * @param value
     *      filed value
     * @return
     *      reference to self
     */
    public Update addToSet(String key, Object value) {
        return update("$addToSet", key, value);
    }

    /**
     * Builder pattern
     *
     * @param key
     *      field name
     * @param value
     *      filed value
     * @return
     *      reference to self
     */
    public Update rename(String key, Object value) {
        return update("$rename", key, value);
    }

    /**
     * Builder pattern
     *
     * @param fields
     *      fields to rename
     * @return
     *      reference to self
     */
    public Update updateCurrentDate(String... fields) {
        Arrays.stream(fields).forEach(key -> update("$currentDate", key, true));
        return this;
    }

    /**
     * Builder pattern
     *
     * @param fields
     *      fields to rename
     * @return
     *      reference to self
     */
    public Update updateMul(Map<String, Double> fields) {
        fields.forEach((key, value) -> update("$mul", key, value));
        return this;
    }

    /**
     * Builder pattern
     *
     * @param fields
     *      fields to rename
     * @return
     *      reference to self
     */
    public Update updateSetOnInsert(Map<String, Object> fields) {
        fields.forEach((key, value) -> update("$setOnInsert", key, value));
        return this;
    }

    /**
     * Builder pattern
     *
     * @param operation
     *      operation on update
     * @param key
     *      field name
     * @param value
     *      filed value
     * @return
     *      reference to self
     */
    @SuppressWarnings("unchecked")
    private Update update(String operation, String key, Object value) {
        documentMap.computeIfAbsent(operation, k -> new LinkedHashMap<>());
        ((Map<String, Object>) documentMap.get(operation)).put(key, value);
        return this;
    }


}
