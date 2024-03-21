package com.datastax.astra.client.model.update;

import lombok.Data;

@Data
public class UpdateManyResult {

    /**
     * Number of matched documents
     */
    private Integer matchedCount;

    /**
     * Number of modified documents
     */
    private Integer modifiedCount;

    /**
     * Populated if upserted
     */
    private Object upsertedId;

    /**
     * Not used any more
     */
    private Boolean moreData;

    /**
     * Next page state.
     */
    private String nextPageState;
}
