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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder to compose filters.
 */
public class FilterBuilderList extends Filter {

    /** Required field name. */
    private final String keyword;

    /**
     * Parent filter
     */
    private final Filter parentFilter;

    /**
     * Constructor.
     *
     * @param parentFilter
     *      parent filter
     * @param key
     *      type of filter ($or, $and)
     */
    public FilterBuilderList(Filter parentFilter, String key) {
        this.keyword = key;
        this.parentFilter = parentFilter;
        this.documentMap = new LinkedHashMap<>();
        documentMap.put(key, new ArrayList<Map<String, Object>>());
    }

    /**
     * Build a Where clause.
     *
     * @param field
     *      current field name
     * @param op
     *      current condition
     * @param result
     *      value for the condition
     * @return
     *      current reference
     */
    public FilterBuilderList where(String field, FilterOperator op, Object result) {
        Map<String, Object> map = new HashMap<>();
        map.put(field, Map.of(op.getOperator(), result));
        getConditions().add(map);
        return this;
    }

    /**
     * Close the loop.
     *
     * @return
     *      a filter
     */
    public Filter end() {
        // Keywords with multiple branches
        if ("$and".equals(keyword) || "$or".equals(keyword)) {
            if (getConditions().size() < 2) {
                throw new IllegalArgumentException(keyword + " should have more than 1 condition");
            }
            if (parentFilter instanceof FilterBuilderList) {
                ((FilterBuilderList) parentFilter).getConditions().add(documentMap);
            } else {
                parentFilter.documentMap.putAll(documentMap);
            }
        }

        // Keywords with single branches
        if ("$not".equals(keyword)) {
            if (getConditions().size() != 1) {
                throw new IllegalArgumentException(keyword + " should have a single condition");
            }
            Map<String, Object> val = new HashMap<>();
            val.put(keyword, getConditions().get(0));
            if (parentFilter instanceof FilterBuilderList) {
                ((FilterBuilderList) parentFilter).getConditions().add(val);
            } else {
                parentFilter.documentMap.putAll(val);
            }
        }
        return parentFilter;
    }

    /**
     * Access conditions are a list.
     *
     * @return
     *      filter keyword.
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getConditions() {
        return ((List<Map<String, Object>>) documentMap.get(keyword));
    }
}
