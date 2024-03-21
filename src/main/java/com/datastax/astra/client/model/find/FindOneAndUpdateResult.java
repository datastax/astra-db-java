package com.datastax.astra.client.model.find;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FindOneAndUpdateResult<DOC> {

    /**
     * Replacement if provided
     */
    private DOC document;

    /**
     * Number of matched documents
     */
    private Integer matchedCount;

    /**
     * Number of modified documents
     */
    private Integer modifiedCount;

    /**
     * Upsert Id if document present
     */
    private Integer upsertedId;


}
