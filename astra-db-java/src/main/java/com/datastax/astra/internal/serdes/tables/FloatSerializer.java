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

/**
 * A custom serializer for {@link Float} values, extending {@link StdSerializer}.
 * This serializer handles special floating-point values, converting them into their
 * textual representations in JSON.
 *
 * <p>Special handling is provided for:</p>
 * <ul>
 *   <li>{@link Float#POSITIVE_INFINITY} and {@link Float#MAX_VALUE}: Serialized as {@code "Infinity"}.</li>
 *   <li>{@link Float#NEGATIVE_INFINITY} and {@code -Float.MAX_VALUE}: Serialized as {@code "-Infinity"}.</li>
 *   <li>{@link Float#NaN}: Serialized as {@code "NaN"}.</li>
 *   <li>All other valid {@link Float} values: Serialized as numeric values.</li>
 * </ul>
 */
public class FloatSerializer extends StdSerializer<Float> {

    /**
     * Default constructor. Initializes the serializer for {@link Float} type.
     */
    public FloatSerializer() {
        super(Float.class);
    }

    /**
     * Serializes a {@link Float} value into JSON.
     * <p>
     * Converts special floating-point values into their respective textual representations:</p>
     * <ul>
     *   <li>{@code "Infinity"} for {@link Float#POSITIVE_INFINITY} and {@link Float#MAX_VALUE}.</li>
     *   <li>{@code "-Infinity"} for {@link Float#NEGATIVE_INFINITY} and {@code -Float.MAX_VALUE}.</li>
     *   <li>{@code "NaN"} for {@link Float#NaN}.</li>
     *   <li>Other numeric values are serialized as standard numbers.</li>
     * </ul>
     *
     * @param value    the {@link Float} value to serialize.
     * @param gen      the {@link JsonGenerator} used to write JSON content.
     * @param provider the {@link SerializerProvider} for accessing serialization configuration.
     * @throws IOException if an I/O error occurs during serialization.
     */
    @Override
    public void serialize(Float value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value.equals(Float.POSITIVE_INFINITY) ||
                value.equals(Float.MAX_VALUE)) {
            gen.writeString("Infinity");
        } else if (value.equals(Float.NEGATIVE_INFINITY) ||
                value.equals(-Float.MAX_VALUE)) {
            gen.writeString("-Infinity");
        } else if (value.isNaN()) {
            gen.writeString("NaN");
        } else {
            gen.writeNumber(value);
        }
    }
}
