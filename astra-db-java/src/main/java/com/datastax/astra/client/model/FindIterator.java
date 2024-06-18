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

import lombok.Getter;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Implementing a custom iterator that will load next page if needed when hitting the last item of page.
 *
 * @param <T>
 *     working document
 */
@Getter
public class FindIterator<T> implements Iterator<T> {

    /** Iterable for both find and distinct. */
    private final PageableIterable<T> parentIterable;

    /** Progress on the current page. */
    private int availableWithoutFetch;

    /** Iterator on current document page. */
    private Iterator<T> resultsIterator;

    /**
     * Starting the cursor on an iterable to fetch more pages.
     *
     * @param findIterable
     *      iterable
     */
    public FindIterator(PageableIterable<T> findIterable) {
        this.parentIterable        = findIterable;
        this.availableWithoutFetch = findIterable.getCurrentPage().getResults().size();
        this.resultsIterator       = findIterable.getCurrentPage().getResults().iterator();
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasNext() {
        boolean hasNext =  (resultsIterator.hasNext() || parentIterable.getCurrentPage().getPageState().isPresent());
        if (!hasNext) {
            parentIterable.close();
        }
        return hasNext;
    }

    /**
     * Implementing a logic of iterator combining current page and paging. An local iterator is started on elements
     * of the processing page. If the local iterator is exhausted, the flag 'nextPageState' can tell us is there are
     * more elements to retrieve. if 'nextPageState' is not null the next page is fetch at Iterable level and the
     * local iterator is reinitialized on the new page.
     *
     * @return
     *      next document in the iterator
     */
    @Override
    public T next() {
        if (resultsIterator.hasNext()) {
            availableWithoutFetch--;
            parentIterable.active = true;
            parentIterable.getTotalItemProcessed().incrementAndGet();
            return resultsIterator.next();
        } else if (parentIterable.getCurrentPage().getPageState().isPresent()) {
            parentIterable.fetchNextPage();
            this.availableWithoutFetch = parentIterable.getCurrentPage().getResults().size();
            this.resultsIterator = parentIterable.getCurrentPage().getResults().iterator();
            return next();
        }
        parentIterable.active    = false;
        parentIterable.exhausted = true;
        throw new NoSuchElementException("Current page is exhausted and no new page available");
    }

    /**
     * Gets the number of results available locally without blocking, which may be 0.
     *
     * <p>
     * If the cursor is known to be exhausted, returns 0.  If the cursor is closed before it's been exhausted, it may return a non-zero
     * value.
     * </p>
     *
     * @return the number of results available locally without blocking
     * @since 4.4
     */
    public int available() {
        return availableWithoutFetch;
    }
}
