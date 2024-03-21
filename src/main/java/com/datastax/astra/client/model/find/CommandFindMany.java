package com.datastax.astra.client.model.find;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.stargate.sdk.data.client.model.Filter;
import io.stargate.sdk.http.domain.FilterKeyword;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Json Api Query Payload Wrapper.
 */
@Getter @Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommandFindMany {

    /**
     * Select.
     */
    private Map<String, Object> projection;

    /**
     * where clause
     */
    private Filter filter;

    /**
     * Order by.
     */
    private Map<String, Object> sort;

    /**
     * Options.
     */
    private Map<String, Object> options;

    /**
     * Default constructor.
     */
    public CommandFindMany() {}

    /**
     * Build a SQL query with a filter (no projection).
     *
     * @param pFilter
     *      current filter
     */
    public CommandFindMany(Filter pFilter) {
        if (pFilter != null) {
            filter = new Filter();
            filter.putAll(pFilter);
        }
    }

    /**
     * Build a query with a filter (no projection).
     *
     * @param pFilter
     *      current filter
     * @param vector
     *      semantic search
     */
    public CommandFindMany(float[] vector, Filter pFilter) {
        if (vector != null) {
            sort = new HashMap<>();
            sort.put(FilterKeyword.VECTOR.getKeyword(), vector);
        }
        if (pFilter != null) {
            filter = new Filter();
            filter.putAll(pFilter);
        }
    }


    /**
     * Look for pageSize.
     *
     * @return
     *      page size
     */
    @JsonIgnore
    public Optional<Integer> getLimit() {
        if (options != null && options.containsKey("limit")) {
            return Optional.ofNullable((Integer) options.get("limit"));
        }
        return Optional.empty();
    }

    /**
     * Update page state
     *
     * @param pageState
     *      new value for page state
     */
    @JsonIgnore
    public void setPageState(String pageState) {
        if (options == null) {
            options = new HashMap<>();
        }
        options.put("pagingState", pageState);
    }

}
