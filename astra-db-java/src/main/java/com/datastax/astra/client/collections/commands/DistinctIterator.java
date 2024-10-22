package com.datastax.astra.client.collections.commands;

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

import com.datastax.astra.client.collections.documents.Document;
import com.datastax.astra.client.core.paging.PageableIterable;
import com.datastax.astra.internal.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Iterator to retrieve distinct values of a field in a document.
 * @param <T>
 *      working class representing the document
 * @param <F>
 *     working class representing the field to extract from the document.
 */
@Slf4j
public class DistinctIterator<T, F> implements Iterator<F> {

    /** Iterable for both find and distinct. */
    private final PageableIterable<T> parentIterable;

    /** Iterator on current document page. */
    private Iterator<F> resultsIterator;

    /** The name of the field. */
    private final String fieldName;

    /** The class in use. */
    private final Class<F> fieldClass;

    private Set<F> currentPageRecords;

    /** Existing values. */
    private final Set<F> existingValues = new HashSet<>();

    /**
     * Starting the cursor on an iterable to fetch more pages.
     *
     * @param findIterable
     *      iterable
     * @param fieldName
     *     name of the field to pick
     * @param fieldClass
     *      type of the field to pick
     */
    public DistinctIterator(PageableIterable<T> findIterable, String fieldName, Class<F> fieldClass) {
        this.parentIterable        = findIterable;
        this.fieldName             = fieldName;
        this.fieldClass            = fieldClass;
        initResultIterator();

    }

    /**
     * Mapping of the document to expected field.
     *
     * @param t
     *      current document
     * @return
     *      extraction of field from document
     */
    private F extractField(T t) {
        return JsonUtils.convertValue(t, Document.class).get(fieldName, fieldClass);
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasNext() {
        boolean hasNext = resultsIterator.hasNext()|| parentIterable.getCurrentPage().getPageState().isPresent();
        if (!hasNext) {
            parentIterable.close();
        }
        return hasNext;
    }

    /**
     * Implementing a logic of iterator combining current page and paging. A local iterator is started on elements
     * of the processing page. If the local iterator is exhausted, the flag 'nextPageState' can tell us is there are
     * more elements to retrieve. if 'nextPageState' is not null the next page is fetch at Iterable level and the
     * local iterator is reinitialized on the new page.
     *
     * @return
     *      next document in the iterator
     */
    @Override
    public F next() {
        if (resultsIterator.hasNext()) {
            parentIterable.getTotalItemProcessed().incrementAndGet();
            F nextValue = resultsIterator.next();
            existingValues.add(nextValue);
            return nextValue;
        } else if (parentIterable.getCurrentPage().getPageState().isPresent()) {
            parentIterable.fetchNextPage();
            initResultIterator();
            // last page is empty after deduplication
            // NoSuchElementException()
            if (currentPageRecords.isEmpty()) {
                log.warn("Last page is empty after deduplication => NoSuchElementException");
            }
            return next();
        }
        parentIterable.close();
        throw new NoSuchElementException("End of the collection");
    }

    /**
     * Items of a page are extracted, deduplicated, then the local resultsIterator is initialized
     * with the items not already processed.
     */
    private void initResultIterator() {
        currentPageRecords = new LinkedHashSet<>();
        currentPageRecords.addAll(parentIterable.getCurrentPage().getResults()
                .stream().map(this::extractField)
                .collect(Collectors.toList()));
        currentPageRecords.removeAll(existingValues);

        this.resultsIterator = currentPageRecords.iterator();
    }
}
