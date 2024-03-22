package com.datastax.astra.client.model.find;

import com.datastax.astra.client.model.Document;
import com.datastax.astra.internal.utils.Assert;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class FindOneAndDeleteOptions {

    /**
     * Order by.
     */
    private Document sort;

    /**
     * Select.
     */
    private Map<String, Integer> projection;

    /**
     * Fluent api.
     *
     * @param pProjection
     *      add a project field
     * @return
     *      current command.
     */
    public FindOneAndDeleteOptions projection(Map<String, Integer> pProjection) {
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
    public FindOneAndDeleteOptions sort(Document pSort) {
        Assert.notNull(pSort, "projection");
        if (this.sort == null) {
            sort = new Document();
        }
        this.sort.putAll(pSort);
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
    public FindOneAndDeleteOptions sortByVector(float[] vector) {
        return sort(new Document().append(Document.VECTOR, vector));
    }


}
