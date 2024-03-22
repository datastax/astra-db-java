package com.datastax.astra.client.model.iterable;

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
import com.datastax.astra.client.model.filter.Filter;
import com.datastax.astra.client.model.find.FindOptions;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;


/**
 * Iterator to get all distinct value for a particular field.
 *
 * @param <FIELD>
 *     type of the field we are looping on.
 */
@Slf4j
public class DistinctIterable<DOC, FIELD> extends PageableIterable<DOC> implements Iterable<FIELD> {

    /** The name of the field. */
    private final String fieldName;

    /** The class in use. */
    private final Class<FIELD> fieldClass;

    /** Iterator on fields. */
    protected DistinctIterator<DOC, FIELD> currentPageIterator;

    /**
     * Constructor for a cursor over the elements of the find.
     * @param collection
     *      source collection client, use to fetch next pages
     * @param filter
     *      original filter used to renew the query
     * @param fieldClass
     *      type some the value
     */
    public DistinctIterable(Collection<DOC> collection, String fieldName, Filter filter, Class<FIELD> fieldClass) {
        this.collection  = collection;
        this.filter      = filter;
        this.fieldName   = fieldName;
        this.fieldClass  = fieldClass;
        // Default and no extra filters.
        this.options     = new FindOptions();
    }

    /** {@inheritDoc} */
    @Override @NonNull
    public DistinctIterator<DOC, FIELD> iterator() {
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
    public List<FIELD> all() {
        if (exhausted) throw new IllegalStateException("Iterable is already exhausted.");
        if (active)    throw new IllegalStateException("Iterable has already been started");
        List<FIELD> results = new ArrayList<>();
        try {
            for (FIELD fieldValue : this) results.add(fieldValue);
        } catch (NoSuchElementException e) {
            log.warn("Last page was empty");
        }
        return results;
    }

}
