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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
/**
 * A custom deserializer for {@link Float} values, extending {@link StdDeserializer}.
 * This deserializer handles special cases for floating-point values represented as strings
 * in JSON, converting them into their corresponding {@link Float} constants.
 *
 * <p>Special handling is provided for:</p>
 * <ul>
 *   <li>{@code "Infinity"}: Deserialized as {@link Float#POSITIVE_INFINITY}.</li>
 *   <li>{@code "-Infinity"}: Deserialized as {@link Float#NEGATIVE_INFINITY}.</li>
 *   <li>{@code "NaN"}: Deserialized as {@link Float#NaN}.</li>
 *   <li>All other valid numeric strings: Parsed into a {@link Float} using {@link Float#parseFloat(String)}.</li>
 * </ul>
 */
public class FloatDeserializer extends StdDeserializer<Float> {

    /**
     * Default constructor. Initializes the deserializer for {@link Float} type.
     */
    public FloatDeserializer() {
        super(Float.class);
    }

    /**
     * Deserializes a JSON string into a {@link Float} object.
     * <p>
     * Converts special floating-point string representations into their corresponding
     * {@link Float} constants. For other valid numeric strings, it parses the value into
     * a {@link Float}.
     * </p>
     *
     * @param p     the {@link JsonParser} providing access to the JSON content.
     * @param ctxt  the {@link DeserializationContext} for contextual information.
     * @return the deserialized {@link Float} value.
     * @throws IOException if an I/O error occurs or the input string is invalid.
     */
    @Override
    public Float deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String text = p.getText();
        return switch (text) {
            case "Infinity" -> Float.POSITIVE_INFINITY;
            case "-Infinity" -> Float.NEGATIVE_INFINITY;
            case "NaN" -> Float.NaN;
            default -> Float.parseFloat(text);
        };
    }
}
