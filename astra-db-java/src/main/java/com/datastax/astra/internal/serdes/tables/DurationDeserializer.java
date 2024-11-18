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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.datastax.astra.client.core.options.DataAPIClientOptions.encodeDurationAsISO8601;

public class DurationDeserializer extends JsonDeserializer<Duration> {

    private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+)([a-zA-Zµ]+)");
    private static final Pattern NEGATIVE_PATTERN = Pattern.compile("^-(.*)");

    @Override
    public Duration deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        String text = p.getText().trim();
        if (text.isEmpty()) {
            return null;
        }

        if (encodeDurationAsISO8601) {
            return Duration.parse(text);
        }

        boolean negative = false;
        Matcher negativeMatcher = NEGATIVE_PATTERN.matcher(text);
        if (negativeMatcher.matches()) {
            negative = true;
            text = negativeMatcher.group(1);
        }

        Matcher matcher = DURATION_PATTERN.matcher(text);
        long totalSeconds = 0;
        long totalNanos = 0;

        int index = 0;
        while (matcher.find(index)) {
            String numberStr = matcher.group(1);
            String unit = matcher.group(2).toLowerCase();

            long number = Long.parseLong(numberStr);

            switch (unit) {
                case "h":
                    totalSeconds += number * 3600;
                    break;
                case "m":
                    totalSeconds += number * 60;
                    break;
                case "s":
                    totalSeconds += number;
                    break;
                case "ms":
                    totalNanos += number * 1_000_000;
                    break;
                case "us":
                case "µs":
                    totalNanos += number * 1_000;
                    break;
                case "ns":
                    totalNanos += number;
                    break;
                default:
                    throw new IOException("Invalid duration unit: " + unit);
            }

            index = matcher.end();
        }

        // Check for invalid format
        if (index != text.length()) {
            throw new IOException("Invalid duration format: " + text);
        }

        // Normalize nanoseconds
        totalSeconds += totalNanos / 1_000_000_000;
        totalNanos %= 1_000_000_000;

        Duration duration = Duration.ofSeconds(totalSeconds, totalNanos);
        if (negative) {
            duration = duration.negated();
        }

        return duration;
    }
}
