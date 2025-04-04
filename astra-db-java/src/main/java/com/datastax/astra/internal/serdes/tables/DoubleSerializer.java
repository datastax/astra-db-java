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
 * A custom serializer for {@link Double} values, extending {@link StdSerializer}.
 * This serializer handles special cases of floating-point numbers, converting them
 * to their textual representations in JSON.
 *
 * <p>Special handling is provided for:</p>
 * <ul>
 *   <li>{@link Double#POSITIVE_INFINITY} and {@link Double#MAX_VALUE}: serialized as "Infinity".</li>
 *   <li>{@link Double#NEGATIVE_INFINITY} and {@code -Double.MAX_VALUE}: serialized as "-Infinity".</li>
 *   <li>{@link Double#NaN}: serialized as "NaN".</li>
 *   <li>All other {@link Double} values: serialized as standard numeric values.</li>
 * </ul>
 */
public class DoubleSerializer extends StdSerializer<Double> {

    /**
     * Default constructor. Initializes the serializer for {@link Double} type.
     */
    public DoubleSerializer() {
        super(Double.class);
    }

    /**
     * Serializes a {@link Double} value into JSON.
     * <p>
     * Converts special floating-point values into their respective textual representations:</p>
     * <ul>
     *   <li>"Infinity" for {@link Double#POSITIVE_INFINITY} and {@link Double#MAX_VALUE}.</li>
     *   <li>"-Infinity" for {@link Double#NEGATIVE_INFINITY} and {@code -Double.MAX_VALUE}.</li>
     *   <li>"NaN" for {@link Double#NaN}.</li>
     *   <li>Other numeric values are serialized as numbers.</li>
     * </ul>
     *
     * @param value    the {@link Double} value to serialize.
     * @param gen      the {@link JsonGenerator} used to write JSON content.
     * @param provider the {@link SerializerProvider} for accessing serialization configuration.
     * @throws IOException if an I/O error occurs during serialization.
     */
    @Override
    public void serialize(Double value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value.equals(Double.POSITIVE_INFINITY) ||
                value.equals(Double.MAX_VALUE)) {
            gen.writeString("Infinity");
        } else if (value.equals(Double.NEGATIVE_INFINITY) ||
                value.equals(-Double.MAX_VALUE)) {
            gen.writeString("-Infinity");
        } else if (value.isNaN()) {
            gen.writeString("NaN");
        } else {
            gen.writeNumber(value);
        }
    }

}
