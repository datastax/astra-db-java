package com.datastax.astra.langchain4j.store.embedding;

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

import dev.langchain4j.store.embedding.filter.comparison.IsEqualTo;
import dev.langchain4j.store.embedding.filter.comparison.IsGreaterThan;
import dev.langchain4j.store.embedding.filter.comparison.IsGreaterThanOrEqualTo;
import dev.langchain4j.store.embedding.filter.comparison.IsIn;
import dev.langchain4j.store.embedding.filter.comparison.IsLessThan;
import dev.langchain4j.store.embedding.filter.comparison.IsLessThanOrEqualTo;
import dev.langchain4j.store.embedding.filter.comparison.IsNotEqualTo;
import dev.langchain4j.store.embedding.filter.comparison.IsNotIn;
import dev.langchain4j.store.embedding.filter.logical.And;
import dev.langchain4j.store.embedding.filter.logical.Not;
import dev.langchain4j.store.embedding.filter.logical.Or;
import com.datastax.astra.client.core.query.Filter;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;

import static com.datastax.astra.client.core.query.Filters.and;
import static com.datastax.astra.client.core.query.Filters.eq;
import static com.datastax.astra.client.core.query.Filters.gt;
import static com.datastax.astra.client.core.query.Filters.gte;
import static com.datastax.astra.client.core.query.Filters.in;
import static com.datastax.astra.client.core.query.Filters.lt;
import static com.datastax.astra.client.core.query.Filters.lte;
import static com.datastax.astra.client.core.query.Filters.ne;
import static com.datastax.astra.client.core.query.Filters.nin;
import static com.datastax.astra.client.core.query.Filters.not;
import static com.datastax.astra.client.core.query.Filters.or;

/**
 * This class is responsible for mapping {@link com.datastax.astra.client.core.query.Filter} objects into AstraDB filter expressions.
 */
class AstraDbFilterMapper {

    /**
     * Maps a {@link dev.langchain4j.store.embedding.filter.Filter} object into an AstraDB {@link Filter} object.
     *
     * @param filter
     *          A {@link dev.langchain4j.store.embedding.filter.Filter} object to map.
     * @return
     *          An AstraDB {@link Filter} object.
     */
    static Filter map(dev.langchain4j.store.embedding.filter.Filter filter) {
        if (filter instanceof IsEqualTo) {
            IsEqualTo f = (IsEqualTo) filter;
            return eq(f.key(), f.comparisonValue());
        } else if (filter instanceof IsNotEqualTo) {
            IsNotEqualTo f = (IsNotEqualTo) filter;
            return ne(f.key(), f.comparisonValue());
        } else if (filter instanceof IsIn) {
            IsIn f = (IsIn) filter;
            return in(f.key(), f.comparisonValues().toArray());
        } else if (filter instanceof IsNotIn) {
            IsNotIn f = (IsNotIn) filter;
            return nin(f.key(), f.comparisonValues().toArray());
        } else if (filter instanceof And) {
            And and = (And) filter;
            return and(map(and.left()), map(and.right()));
        } else if (filter instanceof Not) {
            Not not = (Not) filter;
            return not(map(not.expression()));
        } else if (filter instanceof Or) {
            Or or = (Or) filter;
            return or(map(or.left()), map(or.right()));
        } else if (filter instanceof IsGreaterThan) {
            return mapGreaterThan((IsGreaterThan) filter);
        } else if (filter instanceof IsGreaterThanOrEqualTo) {
            return mapGreaterThanOrEqual((IsGreaterThanOrEqualTo) filter);
        } else if (filter instanceof IsLessThan) {
            return mapLessThan((IsLessThan) filter);
        } else if (filter instanceof IsLessThanOrEqualTo) {
            return mapLessThanOrEqual((IsLessThanOrEqualTo) filter);
        } else {
            throw new UnsupportedOperationException("Unsupported filter type: " + filter.getClass().getName());
        }
    }

    /**
     * Maps an {@link IsEqualTo} filter into an AstraDB filter.
     *
     * @param isGreaterThan
     *        An {@link IsGreaterThan} filter to map.
     * @return
     *       An AstraDB filter.
     */
    private static Filter mapGreaterThan(IsGreaterThan isGreaterThan) {
        Object value = isGreaterThan.comparisonValue();
        if (value instanceof Date) {
            return gt(isGreaterThan.key(), (Date) value);
        } else if (value instanceof Instant) {
            return gt(isGreaterThan.key(), (Instant) value);
        } else if (value instanceof Calendar) {
            return gt(isGreaterThan.key(), (Calendar) value);
        } else  if (value instanceof Number) {
            return gt(isGreaterThan.key(), (Number) value);
        }
        throw new UnsupportedOperationException("Operation 'GreaterThen' not supported for value type: " + value.getClass().getName());
    }

    /**
     * Maps an {@link IsGreaterThanOrEqualTo} filter into an AstraDB filter.
     *
     * @param isGreaterThanOrEqualTo
     *      An {@link IsGreaterThanOrEqualTo} filter to map.
     * @return
     *     An AstraDB filter.
     */
    private static Filter mapGreaterThanOrEqual(IsGreaterThanOrEqualTo isGreaterThanOrEqualTo) {
        Object value = isGreaterThanOrEqualTo.comparisonValue();
        if (value instanceof Date) {
            return gte(isGreaterThanOrEqualTo.key(), (Date) value);
        } else if (value instanceof Instant) {
            return gte(isGreaterThanOrEqualTo.key(), (Instant) value);
        } else if (value instanceof Calendar) {
            return gte(isGreaterThanOrEqualTo.key(), (Calendar) value);
        } else  if (value instanceof Number) {
            return gte(isGreaterThanOrEqualTo.key(), (Number) value);
        }
        throw new UnsupportedOperationException("Operation 'GreaterThanOrEqual' not supported for value type: " + value.getClass().getName());
    }

    /**
     * Maps an {@link IsLessThan} filter into an AstraDB filter.
     *
     * @param isLessThan
     *    An {@link IsLessThan} filter to map.
     * @return
     *   An AstraDB filter.
     */
    private static Filter mapLessThan(IsLessThan isLessThan) {
        Object value = isLessThan.comparisonValue();
        if (value instanceof Date) {
            return lt(isLessThan.key(), (Date) value);
        } else if (value instanceof Instant) {
            return lt(isLessThan.key(), (Instant) value);
        } else if (value instanceof Calendar) {
            return lt(isLessThan.key(), (Calendar) value);
        } else  if (value instanceof Number) {
            return lt(isLessThan.key(), (Number) value);
        }
        throw new UnsupportedOperationException("Operation 'LessThan' not supported for value type: " + value.getClass().getName());
    }

    /**
     * Maps an {@link IsLessThanOrEqualTo} filter into an AstraDB filter.
     *
     * @param isLessThanOrEqualTo
     *    An {@link IsLessThanOrEqualTo} filter to map.
     * @return
     *   An AstraDB filter.
     */
    private static Filter mapLessThanOrEqual(IsLessThanOrEqualTo isLessThanOrEqualTo) {
        Object value = isLessThanOrEqualTo.comparisonValue();
        if (value instanceof Date) {
            return lte(isLessThanOrEqualTo.key(), (Date) value);
        } else if (value instanceof Instant) {
            return lte(isLessThanOrEqualTo.key(), (Instant) value);
        } else if (value instanceof Calendar) {
            return lte(isLessThanOrEqualTo.key(), (Calendar) value);
        } else  if (value instanceof Number) {
            return lte(isLessThanOrEqualTo.key(), (Number) value);
        }
        throw new UnsupportedOperationException("Operation 'LessThanOrEqual' not supported for value type: " + value.getClass().getName());
    }

}

