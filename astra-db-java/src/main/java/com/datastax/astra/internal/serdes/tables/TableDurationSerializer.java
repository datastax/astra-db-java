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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.Duration;
import java.time.Period;

public class TableDurationSerializer extends JsonSerializer<TableDuration> {

    @Override
    public void serialize(TableDuration value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        String durationString = toCompactString(value);
        gen.writeString(durationString);
    }

    private String toCompactString(TableDuration combinedDuration) {
        StringBuilder sb = new StringBuilder();
        boolean negative = combinedDuration.getPeriod().isNegative() || combinedDuration.getDuration().isNegative();

        Period period = combinedDuration.getPeriod().normalized();
        Duration duration = combinedDuration.getDuration();

        if (negative) {
            sb.append('-');
            period = period.negated();
            duration = duration.negated();
        }

        int years = period.getYears();
        int months = period.getMonths();
        int days = period.getDays();

        long hours = duration.toHours();
        duration = duration.minusHours(hours);

        long minutes = duration.toMinutes();
        duration = duration.minusMinutes(minutes);

        long seconds = duration.getSeconds();
        long nanos = duration.getNano();

        long milliseconds = nanos / 1_000_000;
        nanos %= 1_000_000;

        long microseconds = nanos / 1_000;
        nanos %= 1_000;

        // Append period components
        if (years != 0) sb.append(years).append('y');
        if (months != 0) sb.append(months).append("mo");
        if (days != 0) sb.append(days).append('d');

        // Append duration components
        if (hours != 0) sb.append(hours).append('h');
        if (minutes != 0) sb.append(minutes).append('m');
        if (seconds != 0) sb.append(seconds).append('s');
        if (milliseconds != 0) sb.append(milliseconds).append("ms");
        if (microseconds != 0) sb.append(microseconds).append("us");
        if (nanos != 0) sb.append(nanos).append("ns");

        // Handle zero duration
        if (sb.length() == (negative ? 1 : 0)) {
            sb.append('0').append('s');
        }

        return sb.toString();
    }
}
