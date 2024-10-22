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

import com.datastax.astra.client.core.types.DataAPIKeywords;

import java.util.Map;

/**
 * Helper to build a where clause in natural language (fluent API).
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
public class FilterBuilder {

    /** Required field name. */
    private final String fieldName;

    /** Working builder to override the 'where' field and move with builder. */
    private final Filter filter;

    /**
     * Only constructor allowed
     *
     * @param filter
     *  sample filter
     * @param fieldName
     *      field name
     */
    protected FilterBuilder(Filter filter, String fieldName) {
        this.filter    = filter;
        this.fieldName = fieldName;
    }

    /**
     * Syntax sugar.
     * @param cond
     *      conditions
     * @param value
     *      value
     * @return
     *      builder
     */
    private Filter simpleOperator(FilterOperator cond, Object value) {
        filter.documentMap.put(fieldName, Map.of(cond.getOperator(), value));
        return filter;
    }

    /**
     * Syntax sugar.
     *
     * @param key
     *      keyword (size, exists)
     * @param value
     *      value
     * @return
     *      builder
     */
    private Filter simpleKeyword(DataAPIKeywords key, Object value) {
        filter.documentMap.put(fieldName, Map.of(key.getKeyword(), value));
        return filter;
    }

    /**
     * "fieldName": "value" ($eq is omitted)
     *
     * @param value
     *      value
     * @return
     *      self reference
     */
    public Filter isEqualsTo(Object value) {
        filter.documentMap.put(fieldName, value);
        return filter;
    }

    /**
     * $eq: [ ... ]
     *
     * @param value
     *      value
     * @param <V>
     *       the type of the values
     * @return
     *      self reference
     */
    @SafeVarargs
    public final <V> Filter isAnArrayContaining(V... value) {
        return simpleOperator(FilterOperator.EQUALS_TO, value);
    }

    /**
     * $in: [ ... ]
     *
     * @param value
     *      value
     * @param <V>
     *       the type of the values
     * @return
     *      self reference
     */
    public <V> Filter isInArray(V[] value) {
        return simpleOperator(FilterOperator.IN, value);
    }

    /**
     * $in: [ ... ]
     *
     * @param value
     *      value
     * @param <V>
     *       the type of the values
     * @return
     *      self reference
     */
    public <V> Filter isNotInArray(V[] value) {
        return simpleOperator(FilterOperator.NOT_IN, value);
    }

    /**
     * $all: [ ... ]
     *
     * @param value
     *      value
     * @return
     *      self reference
     */
    public Filter isAnArrayExactlyEqualsTo(Object[] value) {
        filter.documentMap.put(fieldName, Map.of(DataAPIKeywords.ALL.getKeyword(), value));
        return filter;
    }

    /**
     * $eq: [ ... ]
     *
     * @param value
     *      value
     * @return
     *      self reference
     */
    public Filter hasSubFieldsEqualsTo(Map<String, Object> value) {
        return simpleOperator(FilterOperator.EQUALS_TO, value);
    }

    /**
     * Add condition is less than.
     *
     * @param value
     *      value
     * @return
     *      self reference
     */
    public Filter isLessThan(Object value) {
        return simpleOperator(FilterOperator.LESS_THAN, value);
    }
    
    /**
     * Add condition is less than.
     *
     * @param value
     *      value
     * @return
     *      self reference
     */
    public Filter isLessOrEqualsThan(Object value) {
        return simpleOperator(FilterOperator.LESS_THAN_OR_EQUALS_TO, value);
    }
    
    /**
     * Add condition is less than.
     *
     * @param value
     *      value
     * @return
     *      self reference
     */        
    public Filter isGreaterThan(Object value) {
        return simpleOperator(FilterOperator.GREATER_THAN, value);
    }
    
    /**
     * Add condition is greater than.
     *
     * @param value
     *      value
     * @return
     *      self reference
     */        
    public Filter isGreaterOrEqualsThan(Object value) {
        return simpleOperator(FilterOperator.GREATER_THAN_OR_EQUALS_TO, value);
    }
    
    /**
     * Add condition is not equals to.
     *
     * @param value
     *      value
     * @return
     *      self reference
     */        
    public Filter isNotEqualsTo(Object value) {
        return simpleOperator(FilterOperator.NOT_EQUALS_TO, value);
    }
    
    /**
     * Add condition exists.
     *
     * @return
     *      self reference
     */
    public Filter exists() {
        return simpleKeyword(DataAPIKeywords.EXISTS, true);
    }
    
    /**
     * Condition to evaluate size
     *
     * @param size
     *      current size value
     * @return
     *      self reference
     */
    public Filter hasSize(int size) {
        return simpleKeyword(DataAPIKeywords.SIZE, size);
    }

}
