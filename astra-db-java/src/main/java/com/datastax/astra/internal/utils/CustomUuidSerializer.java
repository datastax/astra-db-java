package com.datastax.astra.internal.utils;

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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.UUID;

/**
 * Object Id Could be
 * objectId|uuid|uuidv6|uuidv7
 */
public class CustomUuidSerializer extends StdSerializer<UUID> {
    /**
     * Default constructor.
     */
    public CustomUuidSerializer() {
        this(null);
    }

    /**
     * Constructor with type
     * @param t
     *      type
     */
    public CustomUuidSerializer(Class<UUID> t) {
        super(t);
    }

    /** {@inheritDoc} */
    @Override
    public void serialize(UUID uuid, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("$uuid", uuid.toString());
        gen.writeEndObject();
    }
}
