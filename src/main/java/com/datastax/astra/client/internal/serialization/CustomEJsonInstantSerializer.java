package com.datastax.astra.client.internal.serialization;

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
import java.time.Instant;

/**
 * Custom Serializer for EJson Date type.
 */
public class CustomEJsonInstantSerializer extends StdSerializer<Instant> {

    /**
     * Default constructor.
     */
    public CustomEJsonInstantSerializer() {
        this(null);
    }

    /**
     * Constructor with type
     * @param t
     *      type
     */
    public CustomEJsonInstantSerializer(Class<Instant> t) {
        super(t);
    }

    /** {@inheritDoc} */
    @Override
    public void serialize(Instant value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("$date", value.toEpochMilli());
        gen.writeEndObject();
    }
}


