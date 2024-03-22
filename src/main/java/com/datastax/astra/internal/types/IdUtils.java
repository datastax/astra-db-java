package com.datastax.astra.internal.types;

import com.fasterxml.uuid.Generators;

import java.util.UUID;

/**
 * Help to build and consume ids and UUIDS.
 */
public class IdUtils {

    /** Hide constructor for utility classes. */
    private IdUtils() {}

    /**
     * Generate a UUIDv4. (default)
     *
     * @return
     *      uuid v4
     */
    public static UUID generateUUIDv4() {
        return Generators.randomBasedGenerator().generate();
    }

    /**
     * Generate a UUIDv6.
     *
     * @return
     *      uuid v4
     */
    public static UUIDv6 generateUUIDv6() {
        return UUIDv6.generate();
    }

    /**
     * Generate a UUIDv7.
     *
     * @return
     *      uuid v7
     */
    public static UUIDv7 generateUUIDv7() {
        return UUIDv7.generate();
    }


}
