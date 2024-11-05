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

import com.datastax.astra.client.tables.TableDuration;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Duration;
import java.time.Period;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TableDurationDeserializer extends JsonDeserializer<TableDuration> {

    private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+)([a-zA-Zµ]+)");
    private static final Pattern NEGATIVE_PATTERN = Pattern.compile("^-(.*)");

    @Override
    public TableDuration deserialize(JsonParser p, DeserializationContext ctxt)
    throws IOException {
        String text = p.getText().trim();

        if (text.isEmpty()) {
            return null;
        }

        boolean negative = false;
        Matcher negativeMatcher = NEGATIVE_PATTERN.matcher(text);
        if (negativeMatcher.matches()) {
            negative = true;
            text = negativeMatcher.group(1);
        }

        Matcher matcher = DURATION_PATTERN.matcher(text);
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
        if (index != text.length()) {
            throw new IOException("Invalid duration format: " + text);
        }

        if (negative) {
            period = period.negated();
            duration = duration.negated();
        }
        return new TableDuration(period, duration);
    }
}
