package com.datastax.astra.client.model.find;

import io.stargate.sdk.data.client.model.Document;
import io.stargate.sdk.data.client.model.SortOrder;
import io.stargate.sdk.utils.Assert;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * List Options for a FindOne command.
 */
@Getter
public class FindOptions {

    /**
     * Default constructor.
     */
    public FindOptions() {
    }

    /**
     * Order by.
     */
    private Document sort;

    private Map<String, Integer> projection;

    private Integer skip;

    private Integer limit;

    private Boolean includeSimilarity;

    private String pageState;

    /**
     * Fluent api.
     *
     * @return
     *      add a filter
     */
    public FindOptions includeSimilarity() {
        this.includeSimilarity = true;
        return this;
    }

    /**
     * Update page state
     *
     * @param pageState
     *      new value for page state
     */
    public FindOptions withPageState(String pageState) {
        this.pageState = pageState;
        return this;
    }

    /**
     * Fluent api.
     *
     * @param pProjection
     *      add a project field
     * @return
     *      current command.
     */
    public FindOptions projection(Map<String, Integer> pProjection) {
        Assert.notNull(pProjection, "projection");
        if (this.projection == null) {
            this.projection = new LinkedHashMap<>();
        }
        this.projection.putAll(pProjection);
        return this;
    }

    /**
     * Fluent api.
     *
     * @param pSort
     *      add a filter
     * @return
     *      current command.
     */
    public FindOptions sortingBy(Document pSort) {
        Assert.notNull(pSort, "sort");
        if (this.sort == null) {
            sort = new Document();
        }
        this.sort.putAll(pSort);
        return this;
    }

    /**
     * Add a sort clause to the current field.
     *
     * @param fieldName
     *      field name
     * @param ordering
     *      field ordering
     * @return
     *      current reference  find
     */
    public FindOptions sortingBy(String fieldName, SortOrder ordering) {
        return sortingBy(new Document().append(fieldName, ordering.getOrder()));
    }

    /**
     * Help to chain filters.
     *
     * @return
     *      reference to current object
     */
    public FindOptions and() {
        return this;
    }

    /**
     * Add vector in the sort block.
     *
     * @param vector
     *      vector float
     * @return
     *      current command
     */
    public FindOptions sortingByVector(float[] vector) {
        return sortingBy(new Document().append(Document.VECTOR, vector));
    }

    /**
     * Add a skip clause in the find block
     *
     * @param skip
     *      value for skip options
     * @return
     *      current command
     */
    public FindOptions skip(int skip) {
        this.skip = skip;
        return this;
    }

    /**
     * Add a limit clause in the find block
     *
     * @param limit
     *      value for limit options
     * @return
     *      current command
     */
    public FindOptions limit(int limit) {
        this.limit = limit;
        return this;
    }

}
