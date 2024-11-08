package com.datastax.astra.client.tables.row;

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

import com.datastax.astra.client.collections.documents.Document;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Encore the update document
 */
public class TableUpdate extends Row {

    /**
     * Default constructor.
     */
    public TableUpdate() {
        super();
    }

    /**
     * Default constructor.
     *
     * @param json
     *      filter expression as JSON
     */
    @SuppressWarnings("unchecked")
    public TableUpdate(String json) {
        super();
        this.columnMap.putAll(SERIALIZER.unMarshallBean(json, Map.class));
    }

    /**
     * Default constructor.
     *
     * @param obj
     *      filter expression as JSON
     */
    public TableUpdate(Map<String, Object> obj) {
        super();
        this.columnMap.putAll(obj);
    }

    /**
     * Allow to creat a new instance.
     *
     * @return
     *      a new instance
     */
    public static TableUpdate create() {
        return new TableUpdate();
    }

    /**
     * Builder pattern
     *
     * @param fieldName
     *      field name
     * @return
     *      reference to self
     */
    public TableUpdate unset(String fieldName) {
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
    public TableUpdate set(String key, Object value) {
        return update("$set", key, value);
    }


    /**
     * Builder pattern
     *
     * @param row`
     *     new value for row
     * @return
     *      reference to self
     */
    public TableUpdate set(Row row) {
        if (row !=null) row.getColumnMap().forEach(this::set);
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
    private TableUpdate update(String operation, String key, Object value) {
        columnMap.computeIfAbsent(operation, k -> new LinkedHashMap<>());
        ((Map<String, Object>) columnMap.get(operation)).put(key, value);
        return this;
    }


}
