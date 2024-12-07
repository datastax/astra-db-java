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

import com.datastax.astra.client.tables.definition.TableDuration;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Duration;
import java.time.Period;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A custom deserializer for converting JSON strings into {@link TableDuration} objects.
 * Supports both ISO 8601 duration notation (e.g., "P1Y2M10DT2H30M") and compact notation (e.g., "1y2mo10d2h30m").
 * <p>The deserializer attempts to parse the input string in the following order:</p>
 * <ol>
 *     <li>ISO 8601 notation</li>
 *     <li>Compact notation</li>
 * </ol>
 * If the input does not match either format, an {@link IOException} is thrown.
 */
public class TableDurationDeserializer extends JsonDeserializer<TableDuration> {

    /**
     * Regular expression pattern for parsing custom duration strings.
     */
    private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+)([a-zA-Zµ]+)");

    /**
     * Regular expression pattern for parsing negative duration strings.
     */
    private static final Pattern NEGATIVE_PATTERN = Pattern.compile("^-(.*)");

    /**
     * Regular expression pattern for parsing ISO 8601 duration strings.
     */
    private static final Pattern ISO8601_PATTERN = Pattern.compile(
            "P((\\d+)Y)?((\\d+)M)?((\\d+)D)?(T((\\d+)H)?((\\d+)M)?((\\d+)S)?)?"
    );

    /**
     * Default constructor.
     */
    public TableDurationDeserializer() {
    }

    /**
     * Deserializes a JSON string into a {@link TableDuration} object.
     *
     * @param p the {@link JsonParser} providing the JSON input
     * @param ctxt the {@link DeserializationContext} in which the deserializer is operating
     * @return a {@link TableDuration} object representing the parsed duration
     * @throws IOException if the input string is not a valid duration format
     */
    @Override
    public TableDuration deserialize(JsonParser p, DeserializationContext ctxt)
    throws IOException {
        String inputString = p.getText();

        // If nothing is provided, return null
        if (inputString.isEmpty()) {
            return null;
        }
        // Try first as ISO8601
        Matcher matcher = ISO8601_PATTERN.matcher(inputString);
        if (matcher.matches()) {
            return parseIso8601(inputString);
        }

        // Fall back to compact
        return parseCompactNotation(inputString);
    }

    /**
     * Parses a duration string in ISO 8601 notation (e.g., "P1Y2M10DT2H30M").
     *
     * @param inputString the duration string to parse
     * @return a {@link TableDuration} object representing the parsed ISO 8601 duration
     * @throws IllegalArgumentException if the input string does not conform to ISO 8601 duration notation
     */
    private TableDuration parseIso8601(String inputString) {
        Matcher matcher = ISO8601_PATTERN.matcher(inputString);
        int years = matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : 0;
        int months = matcher.group(4) != null ? Integer.parseInt(matcher.group(4)) : 0;
        int days = matcher.group(6) != null ? Integer.parseInt(matcher.group(6)) : 0;

        // Extract duration components
        int hours = matcher.group(9) != null ? Integer.parseInt(matcher.group(9)) : 0;
        int minutes = matcher.group(11) != null ? Integer.parseInt(matcher.group(11)) : 0;
        int seconds = matcher.group(13) != null ? Integer.parseInt(matcher.group(13)) : 0;

        Period period = Period.of(years, months, days);
        Duration duration = Duration.ofHours(hours).plusMinutes(minutes).plusSeconds(seconds);

        return new TableDuration(period, duration);
    }


    /**
     * Parses a duration string in compact notation (e.g., "1y2mo10d2h30m").
     *
     * <p>Compact notation supports the following units:</p>
     * <ul>
     *     <li><b>y</b>: years</li>
     *     <li><b>mo</b>: months</li>
     *     <li><b>w</b>: weeks (7 days)</li>
     *     <li><b>d</b>: days</li>
     *     <li><b>h</b>: hours</li>
     *     <li><b>m</b>: minutes</li>
     *     <li><b>s</b>: seconds</li>
     *     <li><b>ms</b>: milliseconds</li>
     *     <li><b>us</b>/<b>µs</b>: microseconds</li>
     *     <li><b>ns</b>: nanoseconds</li>
     * </ul>
     *
     * @param inputString the duration string to parse
     * @return a {@link TableDuration} object representing the parsed compact duration
     * @throws IOException if the input string contains invalid duration units or format
     */
    private TableDuration parseCompactNotation(String inputString)
    throws IOException {
        boolean negative = false;
        Matcher negativeMatcher = NEGATIVE_PATTERN.matcher(inputString);
        if (negativeMatcher.matches()) {
            negative = true;
            inputString = negativeMatcher.group(1);
        }

        Matcher matcher = DURATION_PATTERN.matcher(inputString);
        Period period = Period.ZERO;
        Duration duration = Duration.ZERO;

        int index = 0;
        while (matcher.find(index)) {
            String numberStr = matcher.group(1);
            String unit = matcher.group(2).toLowerCase();

            long number = Long.parseLong(numberStr);

            switch (unit) {
                case "y":
                    period = period.plusYears(number);
                    break;
                case "mo":
                    period = period.plusMonths(number);
                    break;
                case "w":
                    period = period.plusDays(7 * number);
                    //.plusWeeks(number);
                    break;
                case "d":
                    period = period.plusDays(number);
                    break;
                case "h":
                    duration = duration.plusHours(number);
                    break;
                case "m":
                    duration = duration.plusMinutes(number);
                    break;
                case "s":
                    duration = duration.plusSeconds(number);
                    break;
                case "ms":
                    duration = duration.plusMillis(number);
                    break;
                case "us":
                case "µs":
                    duration = duration.plusNanos(number * 1_000);
                    break;
                case "ns":
                    duration = duration.plusNanos(number);
                    break;
                default:
                    throw new IOException("Invalid duration unit: " + unit);
            }

            index = matcher.end();
        }

        // Check for invalid format
        if (index != inputString.length()) {
            throw new IOException("Invalid duration format: " + inputString);
        }

        if (negative) {
            period = period.negated();
            duration = duration.negated();
        }
        return new TableDuration(period, duration);
    }
}
