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

package com.datastax.astra.client.core.query;

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

import lombok.Getter;

/**
 * Enumeration of filter operators used in Data API query construction.
 * <p>
 * These operators provide a fluent API for building complex query filters,
 * supporting comparison, existence checks, and collection operations.
 * Each operator maps to a corresponding Data API query operator.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * Filter filter = new Filter()
 *     .where("age").isGreaterThan(18)
 *     .where("status").isEqualsTo("active");
 * }
 * </pre>
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Getter
public enum FilterOperator {

    /**
     * Greater than operator ($gt).
     * <p>Matches values that are greater than a specified value.</p>
     */
    GREATER_THAN("$gt"),
    
    /** 
     * Greater than or equal to operator ($gte).
     * <p>Matches values that are greater than or equal to a specified value.</p>
     */  
    GREATER_THAN_OR_EQUALS_TO("$gte"),

    /** 
     * Less than operator ($lt).
     * <p>Matches values that are less than a specified value.</p>
     */
    LESS_THAN("$lt"),

    /**
     * Match operator ($match).
     * <p>Performs text matching operations.</p>
     */
    MATCH("$match"),
    
    /** 
     * Less than or equal to operator ($lte).
     * <p>Matches values that are less than or equal to a specified value.</p>
     */
    LESS_THAN_OR_EQUALS_TO("$lte"),
    
    /** 
     * Equality operator ($eq).
     * <p>Matches values that are equal to a specified value.</p>
     */
    EQUALS_TO("$eq"),
    
    /** 
     * Not equal to operator ($ne).
     * <p>Matches values that are not equal to a specified value.</p>
     */
    NOT_EQUALS_TO("$ne"),
    
    /** 
     * In operator ($in).
     * <p>Matches any of the values specified in an array.</p>
     */
    IN("$in"),

    /**
     * Not in operator ($nin).
     * <p>Matches none of the values specified in an array.</p>
     */
    NOT_IN("$nin"),
    
    /** 
     * Exists operator ($exists).
     * <p>Matches documents that have the specified field.</p>
     */
    EXISTS("$exists"),
    
    /** 
     * Contains operator ($contains).
     * <p>Matches arrays that contain a specified value.</p>
     */
    CONTAINS("$contains"),
    
    /** 
     * Contains key operator ($containsKey).
     * <p>Matches maps that contain a specified key.</p>
     */
    CONTAIN_KEY("$containsKey"),
    
    /** 
     * Contains entry operator ($containsEntry).
     * <p>Matches maps that contain a specified key-value pair.</p>
     */
    CONTAIN_ENTRY("$containsEntry");

    /**
     * Operator name.
     */
    private String operator;
    
    /**
     * Constructor for the enum.
     * @param op
     *      current operator
     */
    FilterOperator(String op) {
        this.operator = op;
    }

}
