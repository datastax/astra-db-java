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
