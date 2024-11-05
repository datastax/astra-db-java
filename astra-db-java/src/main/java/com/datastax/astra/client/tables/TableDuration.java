package com.datastax.astra.client.tables;

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

@Getter
public class TableDuration implements TemporalAmount {

    private final Period period;

    private final Duration duration;

    public TableDuration(Period period, Duration duration) {
        this.period   = period == null ? Period.ZERO : period;
        this.duration = duration == null ? Duration.ZERO : duration;
    }

    public static TableDuration of(Period period, Duration duration) {
        return new TableDuration(period, duration);
    }

    public TableDuration plus(TableDuration other) {
        Period newPeriod = this.period.plus(other.period);
        Duration newDuration = this.duration.plus(other.duration);
        return new TableDuration(newPeriod, newDuration);
    }

    public TableDuration minus(TableDuration other) {
        Period newPeriod = this.period.minus(other.period);
        Duration newDuration = this.duration.minus(other.duration);
        return new TableDuration(newPeriod, newDuration);
    }

    public TableDuration multipliedBy(int scalar) {
        Period newPeriod = this.period.multipliedBy(scalar);
        Duration newDuration = this.duration.multipliedBy(scalar);
        return new TableDuration(newPeriod, newDuration);
    }

    public TableDuration negated() {
        return multipliedBy(-1);
    }

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

    @Override
    public java.util.List<java.time.temporal.TemporalUnit> getUnits() {
        return List.of(ChronoUnit.YEARS, ChronoUnit.MONTHS, ChronoUnit.DAYS, SECONDS, NANOS);
    }

    @Override
    public java.time.temporal.Temporal addTo(java.time.temporal.Temporal temporal) {
        temporal = temporal.plus(period);
        temporal = temporal.plus(duration);
        return temporal;
    }

    @Override
    public java.time.temporal.Temporal subtractFrom(java.time.temporal.Temporal temporal) {
        temporal = temporal.minus(period);
        temporal = temporal.minus(duration);
        return temporal;
    }

}
