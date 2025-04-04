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
 * A custom deserializer for {@link Double} values, extending {@link StdDeserializer}.
 * This class provides special handling for textual representations of special
 * floating-point values such as "Infinity", "-Infinity", and "NaN".
 *
 * <p>The deserializer converts these strings to their corresponding
 * {@link Double} constants: {@link Double#POSITIVE_INFINITY}, {@link Double#NEGATIVE_INFINITY},
 * and {@link Double#NaN}. For any other input string, it attempts to parse the value
 * as a {@link Double} using {@link Double#parseDouble(String)}.</p>
 */
public class DoubleDeserializer extends StdDeserializer<Double> {

    /**
     * Default constructor. Initializes the deserializer for {@link Double} type.
     */
    public DoubleDeserializer() {
        super(Double.class);
    }

    /**
     * Deserializes a JSON string into a {@link Double} object.
     * <p>
     * Special string values such as "Infinity", "-Infinity", and "NaN" are mapped to their respective
     * {@link Double} constants. For other strings, the value is parsed as a {@link Double}.
     * </p>
     *
     * @param p     the {@link JsonParser} providing access to the JSON content.
     * @param ctxt  the {@link DeserializationContext} for contextual information.
     * @return the deserialized {@link Double} value.
     * @throws IOException if an I/O error occurs or if the input is invalid.
     */
    @Override
    public Double deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String text = p.getText();
        return switch (text) {
            case "Infinity" -> Double.POSITIVE_INFINITY;
            case "-Infinity" -> Double.NEGATIVE_INFINITY;
            case "NaN" -> Double.NaN;
            default -> Double.parseDouble(text);
        };
    }

}