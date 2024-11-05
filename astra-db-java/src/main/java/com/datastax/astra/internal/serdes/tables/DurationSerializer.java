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
import java.time.Duration;
import java.util.regex.Pattern;

/**
 * Serialize a date as compact or ISO8601 format.
 */
public class DurationSerializer extends StdSerializer<Duration> {

    private static final Pattern STANDARD_PATTERN = Pattern
            .compile("\\G(\\d+)(y|Y|mo|MO|mO|Mo|w|W|d|D|h|H|s|S|ms|MS|mS|Ms|us|US|uS|Us|µs|µS|ns|NS|nS|Ns|m|M)");
    private static final Pattern ISO8601_PATTERN = Pattern
            .compile("P((\\d+)Y)?((\\d+)M)?((\\d+)D)?(T((\\d+)H)?((\\d+)M)?((\\d+)S)?)?");

    public DurationSerializer() {
        super(Duration.class);
    }

    @Override
    public void serialize(Duration duration, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (duration == null) {
            gen.writeNull();
            return;
        }
        boolean negative = duration.isNegative();
        duration = duration.abs();

        long seconds = duration.getSeconds();
        int nanos    = duration.getNano();

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
