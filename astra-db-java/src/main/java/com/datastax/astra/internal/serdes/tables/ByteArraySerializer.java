package com.datastax.astra.internal.serdes.tables;

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
import java.util.Base64;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.Base64;

/**
 * A custom serializer for byte arrays that encodes the array as a Base64 string
 * and wraps it in a JSON object with a specific field name.
 * <p>
 * This serializer converts a byte array into a JSON object with the following format:
 * </p>
 *
 * <pre>
 * {
 *   "$binary": "base64EncodedString"
 * }
 * </pre>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * ObjectMapper mapper = new ObjectMapper();
 * SimpleModule module = new SimpleModule();
 * module.addSerializer(new ByteArraySerializer());
 * mapper.registerModule(module);
 *
 * byte[] data = {1, 2, 3};
 * String json = mapper.writeValueAsString(data);
 * }
 * </pre>
 */
public class ByteArraySerializer extends StdSerializer<byte[]> {

    /**
     * Default constructor that specifies the {@code byte[]} type for serialization.
     */
    public ByteArraySerializer() {
        super(byte[].class);
    }

    /**
     * Serializes a byte array as a Base64-encoded string wrapped in a JSON object.
     *
     * @param value    the byte array to serialize
     * @param gen      the {@link JsonGenerator} used to write the serialized JSON output
     * @param provider the {@link SerializerProvider} that can be used to get serializers for
     *                 serializing objects contained within this value
     * @throws IOException if an I/O error occurs during serialization
     */
    @Override
    public void serialize(byte[] value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        String base64Encoded = Base64.getEncoder().encodeToString(value);
        gen.writeStartObject();
        gen.writeStringField("$binary", base64Encoded);
        gen.writeEndObject();
    }
}
