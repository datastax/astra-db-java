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

import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.Duration;

/**
 * A custom serializer for {@link Duration} objects, extending {@link StdSerializer}.
 * This serializer supports two serialization formats for durations:
 *
 * <ul>
 *   <li>ISO-8601 format: Encodes the duration as an ISO-8601 string (e.g., {@code "PT1H30M"}).</li>
 *   <li>Compact format: Encodes the duration as a human-readable string with unit suffixes
 *       (e.g., {@code "1h30m"}).</li>
 * </ul>
 *
 * <p>The format used is determined by the {@code isEncodeDurationAsISO8601()} option in
 * {@link DataAPIClientOptions}. If the duration is {@code null}, it serializes as a JSON {@code null}.
 * </p>
 *
 * <p>Supported time units in the compact format:</p>
 * <ul>
 *   <li>{@code h}: Hours</li>
 *   <li>{@code m}: Minutes</li>
 *   <li>{@code s}: Seconds</li>
 *   <li>{@code ms}: Milliseconds</li>
 *   <li>{@code us}: Microseconds</li>
 *   <li>{@code ns}: Nanoseconds</li>
 * </ul>
 *
 * <p>Negative durations are serialized with a {@code -} prefix (e.g., {@code "-1h30m"}).</p>
 */
public class DurationSerializer extends StdSerializer<Duration> {

    /**
     * Default constructor. Initializes the serializer for {@link Duration} type.
     */
    public DurationSerializer() {
        super(Duration.class);
    }

    /**
     * Serializes a {@link Duration} object into JSON.
     * <p>
     * The serialization format depends on the {@code isEncodeDurationAsISO8601()} option
     * in {@link DataAPIClientOptions}. If enabled, the duration is serialized in ISO-8601 format.
     * Otherwise, it is serialized in a compact human-readable format using time unit suffixes.
     * </p>
     *
     * @param duration     the {@link Duration} object to serialize.
     * @param gen          the {@link JsonGenerator} used to write JSON content.
     * @param serializers  the {@link SerializerProvider} for accessing serialization configuration.
     * @throws IOException if an I/O error occurs during serialization.
     */
    @Override
    public void serialize(Duration duration, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (duration == null) {
            gen.writeNull();
            return;
        }

        if (DataAPIClientOptions.getSerdesOptions().isEncodeDurationAsISO8601()) {
            gen.writeString(duration.toString());
            return;
        }

        boolean negative = duration.isNegative();
        duration = duration.abs();

        long seconds = duration.getSeconds();
        int nanos = duration.getNano();

        // Break down the duration into units
        long hours = seconds / 3600;
        seconds %= 3600;

        long minutes = seconds / 60;
        seconds %= 60;

        long milliseconds = nanos / 1_000_000;
        nanos %= 1_000_000;

        long microseconds = nanos / 1_000;
        nanos %= 1_000;

        // Build the duration string
        StringBuilder sb = new StringBuilder();
        if (negative) {
            sb.append('-');
        }
        if (hours != 0) {
            sb.append(hours).append('h');
        }
        if (minutes != 0) {
            sb.append(minutes).append('m');
        }
        if (seconds != 0) {
            sb.append(seconds).append('s');
        }
        if (milliseconds != 0) {
            sb.append(milliseconds).append("ms");
        }
        if (microseconds != 0) {
            sb.append(microseconds).append("us");
        }
        if (nanos != 0) {
            sb.append(nanos).append("ns");
        }

        // Handle zero duration
        String durationString = sb.toString();
        if (durationString.equals("") || (negative && durationString.equals("-"))) {
            durationString = negative ? "-0s" : "0s";
        }
        gen.writeString(durationString);
    }
}
