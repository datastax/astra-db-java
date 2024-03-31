package com.datastax.astra.client.model;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Encode the presence of a field in the result.
 */
public class Projections {

    /**
     * Hide constructor
     */
    private Projections() {}

    /**
     * Include a field in the result.
     *
     * @param field
     *      include field
     * @return
     *      name to include
     */
    public static Projection[] include(String... field) {
        return Arrays.stream(field)
                .map(f -> new Projection(f, true))
                .toArray(Projection[]::new);
    }

    /**
     * Exclude  a field in the result.
     *
     * @param field
     *      field name to exclude
     * @return
     *      list of projection
     */
    public static Projection[] exclude(String... field) {
        return Arrays.stream(field)
                .map(f -> new Projection(f, false))
                .toArray(Projection[]::new);
    }
}
