package com.datastax.astra.client.model;

import lombok.Data;

/**
 * Encode the presence of a field in the result.
 */
@Data
public class Projection {

    /** Name of the field. */
    private String field;

    /** Is the field present in the result. */
    private boolean present;

    /**
     * Default constructor.
     */
    public Projection() {}

}
