package com.datastax.astra.client.model;

/**
 * Helper for Syntax with updates.
 */
public class Updates {

    /**
     * Hide constructor.
     */
    private Updates() {}

    /**
     * Initialize an {@link Update with the key and value.}
     * @param field
     *      field name
     * @param value
     *      new value
     * @return
     *      update object
     */
    public static Update set(String field, Object value) {
        return new Update().set(field, value);
    }

    /**
     * Initialize an {@link Update with the key and value.}
     * @param field
     *      field name
     * @param value
     *      new value
     * @return
     *      update object
     */
    public static Update min(String field, Object value) {
        return new Update().min(field, value);
    }

    /**
     * Initialize an {@link Update with the key and value.}
     * @param field
     *      field name
     * @param value
     *      new value incremented
     * @return
     *      update object
     */
    public static Update inc(String field, Double value) {
        return new Update().inc(field, value);
    }
}
