package com.datastax.astra.client.model.update;

import lombok.Data;

/**
 * Return update result.
 */
@Data
public class UpdateResult {

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

}
