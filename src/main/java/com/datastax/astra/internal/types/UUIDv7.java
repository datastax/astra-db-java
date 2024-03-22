package com.datastax.astra.internal.types;

import com.fasterxml.uuid.Generators;

import java.util.Random;
import java.util.UUID;

/**
 * Materializing the UUIDv6 as a specialization class to drive serialization and deserialization.
 */
public class UUIDv7 {

    private final UUID uuid;

    public UUIDv7(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * Return the Java Utils UUID.
     */
    public UUID toUUID() {
        return uuid;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return toUUID().toString();
    }

    /**
     * Generate from a string.
     *
     * @param strUUID
     *      uuid as a String
     * @return
     *      an instance of UUIDv6
     */
    public static UUIDv7 fromString(String strUUID) {
        return new UUIDv7(UUID.fromString(strUUID));
    }

    /**
     * Generate a new UUIDv6.
     *
     * @return
     *      uuid v6.
     */
    public static UUIDv7 generate() {
        return new UUIDv7(Generators.timeBasedEpochRandomGenerator(new Random()).generate());
    }



}
