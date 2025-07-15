package com.datastax.astra.client.collections.definition.documents.types;

/*-
 * #%L
 * Data API Java Client
 * --
 * Copyright (C) 2024 - 2025 DataStax
 * --
 * Licensed under the Apache License, Version 2.0
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.datastax.astra.internal.serdes.collections.TimeUUIDSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.UUIDUtil;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a time-based UUID (Version 1).
 * <p>
 * This class encapsulates a UUID that is generated based on the current time,
 * allowing for the extraction of the timestamp embedded within the UUID.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * TimeUUID timeUUID = new TimeUUID();
 * Instant timestamp = timeUUID.readInstant();
 * }
 * </pre>
 */
@JsonSerialize(using = TimeUUIDSerializer.class)
public class TimeUUID {

    /**
     * UUID.
     */
    private final UUID uuid;

    /**
     * Default constructor.
     */
    public TimeUUID() {
        // Create a time-based (Version-1) UUID generator
        this(Generators.timeBasedGenerator().generate());
    }

    /**
     * Constructor.
     *
     * @param uuid
     *      uuid
     */
    public TimeUUID(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * Constructor.
     *
     * @param strUUID
     *      uuid
     */
    public TimeUUID(String strUUID) {
        this(UUID.fromString(strUUID));
    }

    /**
     * Return the Java Utils UUID.
     *
     * @return
     *      uuid value
     */
    public UUID toUUID() {
        return uuid;
    }

    /**
     * Extract the hidden timestamp (100-nanosecond units since UUID epoch).
     *
     * @return the raw 60-bit timestamp value embedded in this UUIDv6
     */
    public long readTimeStamp() {
        return UUIDUtil.extractTimestamp(uuid);
    }

    /**
     * Get the timestamp as an {@link Instant}.
     *
     * @return
     *      the timestamp as an Instant
     */
    public Instant readInstant() {
        return Instant.ofEpochMilli(readTimeStamp());
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
    public static UUIDv6 fromString(String strUUID) {
        return new UUIDv6(strUUID);
    }
}
