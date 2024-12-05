package com.datastax.astra.client.tables.commands;

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

import com.datastax.astra.client.tables.definition.rows.Row;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Encore the update document
 */
public class TableUpdateOperation extends Row {

    /**
     * Default constructor.
     */
    public TableUpdateOperation() {
        super();
    }

    /**
     * Default constructor.
     *
     * @param json
     *      filter expression as JSON
     */
    @SuppressWarnings("unchecked")
    public TableUpdateOperation(String json) {
        super();
        this.columnMap.putAll(SERIALIZER.unMarshallBean(json, Map.class));
    }

    /**
     * Default constructor.
     *
     * @param obj
     *      filter expression as JSON
     */
    public TableUpdateOperation(Map<String, Object> obj) {
        super();
        this.columnMap.putAll(obj);
    }

    /**
     * Builder pattern
     *
     * @param fields
     *      fields name
     * @return
     *      reference to self
     */
    public TableUpdateOperation unset(String... fields) {
        if (fields != null) {
            for (String field : fields) {
                update("$unset", field, "");
            }
        }
        return this;
    }
    public TableUpdateOperation unset(String field, Object value) {
        return update("$unset", field, value);
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
    public TableUpdateOperation set(String key, Object value) {
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
    public TableUpdateOperation set(Row row) {
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
    private TableUpdateOperation update(String operation, String key, Object value) {
        columnMap.computeIfAbsent(operation, k -> new LinkedHashMap<>());
        ((Map<String, Object>) columnMap.get(operation)).put(key, value);
        return this;
    }


}
