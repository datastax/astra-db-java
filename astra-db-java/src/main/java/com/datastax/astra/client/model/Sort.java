package com.datastax.astra.client.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Class to help building a projection.
 */
@Data
@AllArgsConstructor
public class Sort {

    /** name of the Projection. */
    String field;

    /** sort for the field. */
    SortOrder sort;

    /** Default constructor. */
    public Sort() {}

    /**
     * Builder for a projection.
     *
     * @param field
     *      name of the field to sort on
     * @param sort
     *      sort clause
     * @return
     *      self reference
     */
    public static Sort of(String field, SortOrder sort) {
        Sort p = new Sort();
        p.field = field;
        p.sort = sort;
        return p;
    }

}