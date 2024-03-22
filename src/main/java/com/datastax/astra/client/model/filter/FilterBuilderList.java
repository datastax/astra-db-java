package com.datastax.astra.client.model.filter;

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
