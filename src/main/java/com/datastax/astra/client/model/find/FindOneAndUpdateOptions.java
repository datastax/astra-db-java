package com.datastax.astra.client.model.find;

import com.datastax.astra.client.model.Document;
import com.datastax.astra.internal.utils.Assert;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class FindOneAndUpdateOptions {

    /**
     * Order by.
     */
    private Document sort;

    /**
     * Select.
     */
    private Map<String, Integer> projection;

    private Boolean upsert;

    private FindOneAndReplaceOptions.ReturnDocument returnDocument = FindOneAndReplaceOptions.ReturnDocument.after;

    /**
     * Fluent api.
     *
     * @param pProjection
     *      add a project field
     * @return
     *      current command.
     */
    public FindOneAndUpdateOptions projection(Map<String, Integer> pProjection) {
        Assert.notNull(pProjection, "projection");
        if (this.projection == null) {
            this.projection = new LinkedHashMap<>();
        }
        this.projection.putAll(pProjection);
        return this;
    }

    public FindOneAndUpdateOptions returnDocument(FindOneAndReplaceOptions.ReturnDocument returnDocument) {
        Assert.notNull(returnDocument, "returnDocument");
        this.returnDocument = returnDocument;
        return this;
    }

    public FindOneAndUpdateOptions upsert(Boolean upsert) {
        Assert.notNull(upsert, "upsert");
        this.upsert = upsert;
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
    public FindOneAndUpdateOptions sort(Document pSort) {
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
    public FindOneAndUpdateOptions sortByVector(float[] vector) {
        return sort(new Document().append(Document.VECTOR, vector));
    }

}
