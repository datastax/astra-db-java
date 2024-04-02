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

import java.util.List;
import java.util.Map;

/**
 * Helper for Syntax with updates.
 */
public class Updates {

    /**
     * Hide constructor.
     */
    private Updates() {}

    /**
     * Initialize an {@link Update with the key and value.}
     * @param field
     *      field name
     * @param value
     *      new value
     * @return
     *      update object
     */
    public static Update set(String field, Object value) {
        return new Update().set(field, value);
    }

    /**
     * Initialize an {@link Update with the key and value.}
     * @param field
     *      field name
     * @param value
     *      new value
     * @return
     *      update object
     */
    public static Update min(String field, Double value) {
        return new Update().min(field, value);
    }

    /**
     * Initialize an {@link Update with the key and value.}
     * @param field
     *      field name
     * @param value
     *      new value incremented
     * @return
     *      update object
     */
    public static Update inc(String field, Double value) {
        return new Update().inc(field, value);
    }

    /**
     * Initialize an {@link Update with the key and value.}
     * @param field
     *      field name
     * @param newName
     *      new name
     * @return
     *      update object
     */
    public static Update rename(String field, String newName) {
        return new Update().rename(field, newName);
    }

    /**
     * Initialize an {@link Update with the key and value.}
     *
     * @param field
     *      field name
     * @return
     *      update object
     */
    public static Update unset(String field) {
        return new Update().unset(field);
    }

    /**
     * Initialize an {@link Update with the key and value.}
     *
     * @param fields
     *      map of fields
     * @return
     *      update object
     */
    public static Update updateSetOnInsert( Map<String, Double> fields) {
        return new Update().updateSetOnInsert(fields);
    }

    /**
     * Initialize an {@link Update with the key and value.}
     *
     * @param fields
     *      map of fields
     * @return
     *      update object
     */
    public static Update updateCurrentDate(String... fields) {
        return new Update().updateCurrentDate(fields);
    }

    /**
     * Initialize an {@link Update with the key and value.}
     *
     * @param key
     *      field name
     * @param value
     *      field value
     * @return
     *      update object
     */
    public static Update addToSet(String key, Object value) {
        return new Update().addToSet(key, value);
    }

    /**
     * Initialize an {@link Update with the key and value.}
     *
     * @param key
     *      field name
     * @param value
     *      field value
     * @return
     *      update object
     */
    public static Update push(String key, Object value) {
        return new Update().push(key, value);
    }

    /**
     * Initialize an {@link Update with the key and value.}
     *
     * @param key
     *      field name
     * @param value
     *      field value
     * @return
     *      update object
     */
    public static Update pop(String key, Object value) {
        return new Update().pop(key, value);
    }

    /**
     * Initialize an {@link Update with the key and value.}
     *
     * @param key
     *      field name
     * @param values
     *      list of field values
     * @param position
     *      position to insert
     * @return
     *      update object
     */
    public static Update pushEach(String key, List<Object> values, Integer position) {
        return new Update().pushEach(key, values, position);
    }

}
