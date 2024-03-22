package com.datastax.astra.client.model.update;

import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.find.SortOrder;
import com.datastax.astra.internal.utils.Assert;
import lombok.Data;

@Data
public class UpdateOneOptions {

    private Boolean upsert;

    /**
     * Order by.
     */
    private Document sort;

    public UpdateOneOptions upsert(Boolean upsert) {
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
    public UpdateOneOptions sortingBy(Document pSort) {
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
    public UpdateOneOptions sortingBy(String fieldName, SortOrder ordering) {
        return sortingBy(new Document().append(fieldName, ordering.getOrder()));
    }
}
