package com.datastax.astra.client.model.delete;

import io.stargate.sdk.data.client.model.Document;
import io.stargate.sdk.data.client.model.SortOrder;
import io.stargate.sdk.utils.Assert;
import lombok.Getter;

/**
 * Options to delete One document.
 */
@Getter
public class DeleteOneOptions {

    /**
     * Default constructor.
     */
    public DeleteOneOptions() {
    }

    /**
     * Order by.
     */
    private Document sort;

    /**
     * Fluent api.
     *
     * @param pSort
     *      add a filter
     * @return
     *      current command.
     */
    public DeleteOneOptions sortingBy(Document pSort) {
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
    public DeleteOneOptions sortingBy(String fieldName, SortOrder ordering) {
        return sortingBy(new Document().append(fieldName, ordering.getOrder()));
    }
}
