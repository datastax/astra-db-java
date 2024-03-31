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
    public static Update min(String field, Object value) {
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
}
