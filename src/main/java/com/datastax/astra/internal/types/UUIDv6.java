package com.datastax.astra.internal.types;

/*-
 * #%L
 * Data API Java Client
 * --
 * Copyright (C) 2024 DataStax
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

import com.fasterxml.uuid.Generators;

import java.util.UUID;

/**
 * Materializing the UUIDv6 as a specialization class to drive serialization and deserialization.
 */
public class UUIDv6 {

    /**
     * UUID.
     */
    private final UUID uuid;

    /**
     * Constructor.
     *
     * @param uuid
     *      uuid
     */
    public UUIDv6(UUID uuid) {
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
    public static UUIDv6 fromString(String strUUID) {
        return new UUIDv6(UUID.fromString(strUUID));
    }

    /**
     * Generate a new UUIDv6.
     *
     * @return
     *      uuid v6.
     */
    public static UUIDv6 generate() {
        return new UUIDv6(Generators.timeBasedReorderedGenerator().generate());
    }



}
