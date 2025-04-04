package com.datastax.astra.client.tables.definition;

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

import lombok.Getter;

import java.time.Duration;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.List;

import static java.time.temporal.ChronoUnit.NANOS;
import static java.time.temporal.ChronoUnit.SECONDS;

/**
 * Represents a combination of a {@link java.time.Period} and a {@link java.time.Duration}.
 * This class is immutable and implements {@link java.time.temporal.TemporalAmount} to
 * support arithmetic operations and integration with temporal objects.
 * <p>
 * The {@code TableDuration} class provides methods to manipulate and retrieve
 * combined period and duration values, as well as utility methods for
 * serialization and formatting.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * TableDuration td1 = TableDuration.of(Period.ofDays(2), Duration.ofHours(5));
 * TableDuration td2 = TableDuration.of(Period.ofMonths(1), Duration.ofMinutes(30));
 *
 * TableDuration combined = td1.plus(td2);
 * System.out.println(combined.toISO8601()); // Prints ISO8601 representation
 * }</pre>
 */
@Getter
public class TableDuration implements TemporalAmount {

    /**
     * The {@link java.time.Period} component of this duration. Non-null, defaults to {@link Period#ZERO}.
     */
    private final Period period;

    /**
     * The {@link java.time.Duration} component of this duration. Non-null, defaults to {@link Duration#ZERO}.
     */
    private final Duration duration;

    /**
     * Constructs a {@code TableDuration} from the specified period and duration.
     * Null values are treated as zero.
     *
     * @param period   the period component, or null for zero period
     * @param duration the duration component, or null for zero duration
     */
    public TableDuration(Period period, Duration duration) {
        this.period   = period == null ? Period.ZERO : period;
        this.duration = duration == null ? Duration.ZERO : duration;
    }

    /**
     * Factory method to create a new {@code TableDuration} instance.
     *
     * @param period   the period component
     * @param duration the duration component
     * @return a new instance of {@code TableDuration}
     */
    public static TableDuration of(Period period, Duration duration) {
        return new TableDuration(period, duration);
    }

    /**
     * Adds the specified {@code TableDuration} to this instance.
     *
     * @param other the other {@code TableDuration} to add
     * @return a new {@code TableDuration} instance representing the result
     */
    public TableDuration plus(TableDuration other) {
        Period newPeriod = this.period.plus(other.period);
        Duration newDuration = this.duration.plus(other.duration);
        return new TableDuration(newPeriod, newDuration);
    }

    /**
     * Subtracts the specified {@code TableDuration} from this instance.
     *
     * @param other the other {@code TableDuration} to subtract
     * @return a new {@code TableDuration} instance representing the result
     */
    public TableDuration minus(TableDuration other) {
        Period newPeriod = this.period.minus(other.period);
        Duration newDuration = this.duration.minus(other.duration);
        return new TableDuration(newPeriod, newDuration);
    }

    /**
     * Multiplies this duration by the specified scalar value.
     *
     * @param scalar the scalar to multiply by
     * @return a new {@code TableDuration} instance representing the result
     */
    public TableDuration multipliedBy(int scalar) {
        Period newPeriod = this.period.multipliedBy(scalar);
        Duration newDuration = this.duration.multipliedBy(scalar);
        return new TableDuration(newPeriod, newDuration);
    }

    /**
     * Negates this duration.
     *
     * @return a new {@code TableDuration} instance with the negated values
     */
    public TableDuration negated() {
        return multipliedBy(-1);
    }

    /** {@inheritDoc} */
    @Override
    public long get(TemporalUnit unit) {
        if (unit == ChronoUnit.YEARS) {
            return getPeriod().getYears();
        } else if (unit == ChronoUnit.MONTHS) {
            return getPeriod().getMonths();
        } else if (unit == ChronoUnit.DAYS) {
            return getPeriod().getDays();
        } else if (unit == SECONDS) {
            return getDuration().getSeconds();
        } else if (unit == NANOS) {
            return getDuration().getNano();
        } else {
            throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
        }
    }

    /** {@inheritDoc} */
    @Override
    public java.util.List<java.time.temporal.TemporalUnit> getUnits() {
        return List.of(ChronoUnit.YEARS, ChronoUnit.MONTHS, ChronoUnit.DAYS, SECONDS, NANOS);
    }

    /** {@inheritDoc} */
    @Override
    public java.time.temporal.Temporal addTo(java.time.temporal.Temporal temporal) {
        temporal = temporal.plus(period);
        temporal = temporal.plus(duration);
        return temporal;
    }

    /** {@inheritDoc} */
    @Override
    public java.time.temporal.Temporal subtractFrom(java.time.temporal.Temporal temporal) {
        temporal = temporal.minus(period);
        temporal = temporal.minus(duration);
        return temporal;
    }

    /**
     * Converts this {@code TableDuration} to an ISO8601 string representation.
     * The format follows the standard for combining periods and durations.
     *
     * @return an ISO8601 string representing this duration
     */
    public String toISO8601() {
        StringBuilder result = new StringBuilder("P");
        // Serialize Period part
        if (period.getYears() != 0) result.append(period.getYears()).append("Y");
        if (period.getMonths() != 0) result.append(period.getMonths()).append("M");
        if (period.getDays() != 0) result.append(period.getDays()).append("D");

        // Serialize Duration part
        if (duration.getSeconds() != 0 || duration.getNano() != 0) {
            result.append("T");
            long hours = duration.toHours();
            long minutes = duration.toMinutes() % 60;
            long seconds = duration.getSeconds() % 60;

            if (hours != 0) result.append(hours).append("H");
            if (minutes != 0) result.append(minutes).append("M");
            if (seconds != 0) result.append(seconds).append("S");
        }

        return result.toString();
    }


    /**
     * Returns a compact string representation of this duration.
     * Negative values are prefixed with a "-" and units are suffixed with appropriate abbreviations.
     * <p>Example: {@code -1y2mo3d4h5m6s} represents negative 1 year, 2 months, etc.</p>
     *
     * @return a compact string representation of this duration
     */
    public String toCompactString() {
        StringBuilder sb = new StringBuilder();
        boolean negative = getPeriod().isNegative() || getDuration().isNegative();
        Period period = getPeriod().normalized();
        Duration duration = getDuration();

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
