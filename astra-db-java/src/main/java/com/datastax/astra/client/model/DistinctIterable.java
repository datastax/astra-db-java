package com.datastax.astra.client.model;

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

import com.datastax.astra.client.Collection;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Iterator to get all distinct value for a particular field.
 *
 * @param <F>
 *     type of the field we are looping on.
 * @param <T>
 *     type of the document used in the associated collection.
 */
@Slf4j
public class DistinctIterable<T, F> extends PageableIterable<T> implements Iterable<F> {

    /** The name of the field. */
    private final String fieldName;

    /** The class in use. */
    private final Class<F> fieldClass;

    /** Iterator on fields. */
    protected DistinctIterator<T, F> currentPageIterator;

    /**
     * Constructs an iterable that provides distinct elements from a specified collection, optionally filtered by
     * a given criterion. This iterable allows for iterating over unique values of a specific field within the collection's documents,
     * which can be particularly useful for data analysis, reporting, or implementing specific business logic that requires
     * uniqueness in the dataset.
     * <p>
     * The distinct elements are determined based on the {@code fieldName} parameter, ensuring that each value provided during
     * iteration is unique with respect to this field across all documents in the collection. The {@code filter} parameter allows
     * for narrowing down the documents considered by this iterable, offering the capability to perform more targeted queries.
     * </p>
     *
     * @param collection The source collection client, used to fetch documents and, if necessary, subsequent pages of results.
     *                   This collection should be capable of executing queries and returning filtered results.
     * @param fieldName The name of the field for which unique values are to be iterated over. This field's values are used
     *                  to determine the distinctness of elements provided by this iterable.
     * @param filter The original filter used to limit the documents considered for finding distinct values. This filter
     *               allows for the specification of criteria that documents must meet to be included in the iteration.
     * @param fieldClass The class of the field values being iterated over. This parameter is used to ensure type safety
     *                   and proper casting of the field values extracted from the documents in the collection.
     */
    public DistinctIterable(Collection<T> collection, String fieldName, Filter filter, Class<F> fieldClass) {
        this.collection  = collection;
        this.filter      = filter;
        this.fieldName   = fieldName;
        this.fieldClass  = fieldClass;
        this.options     = new FindOptions();
    }

    /** {@inheritDoc} */
    @Override @NonNull
    public DistinctIterator<T, F> iterator() {
        if (currentPageIterator == null) {
            active = fetchNextPage();
            this.currentPageIterator = new DistinctIterator<>(this, fieldName, fieldClass);
        }
        return currentPageIterator;
    }

    /**
     * Will exhaust the list and put all value in memory.
     *
     * @return
     *      all values of the iterable
     */
    public List<F> all() {
        if (exhausted) throw new IllegalStateException("Iterable is already exhausted.");
        if (active)    throw new IllegalStateException("Iterable has already been started");
        List<F> results = new ArrayList<>();
        try {
            for (F fieldValue : this) results.add(fieldValue);
        } catch (NoSuchElementException e) {
            log.warn("Last page was empty");
        }
        return results;
    }

}
