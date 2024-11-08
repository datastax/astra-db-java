package com.datastax.astra.client.core.paging;

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

import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.options.CollectionFindOptions;
import com.datastax.astra.client.core.query.Filter;
import lombok.Getter;
import lombok.Setter;

import java.io.Closeable;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Helping Iteration on Pages and Documents for DataApi.
 *
 * @param <T>
 *     type of the document used in the associated collection.
 */
@Getter @Setter
public abstract class PageableIterable<T> implements Closeable {

    // -------- Inputs ---------

    /** Reference to the collection in use. */
    protected Collection<T> collection;

    /** Check host many has been processed (skip and limit support) */
    protected final AtomicInteger totalItemProcessed = new AtomicInteger(0);

    /** The iterable is active and progressing on the results. */
    protected boolean active = false;

    /** the Iterator is exhausted */
    protected boolean exhausted = false;

    /** The current page in use. */
    protected Page<T> currentPage;

    /** Number of items still available to retrieve in this page. */
    protected int currentPageAvailable;

    // ----- Find options ---

    /** Original command, we will edit it to iterate on pages. */
    protected Filter filter;

    /** Original command, we will edit it to iterate on pages. */
    protected CollectionFindOptions options;

    /**
     * Default constructor.
     */
    protected PageableIterable() {
        // left blank as populated internaly
    }

    /**
     * Fetch the next page if the result.
     *
     * @return
     *      if a new page has been found.
     */
    public boolean fetchNextPage() {
        if (currentPage == null || currentPage.getPageState().isPresent()) {
            if (currentPage != null && currentPage.getPageState().isPresent()) {
                options.pageState(currentPage.getPageState().get());
            }
            this.currentPage = collection.findPage(filter, options);
        }
        return false;
    }

    /**
     * Get current page sort Vector When available.
     *
     * @return
     *      sortVector if asked in previous request and iterator is still active.
     */
    public Optional<float[]> getSortVector() {
        if (currentPage == null) {
            throw new IllegalStateException("The iterator is not active and the 'sortVector' " +
                    "cannot be retrieved, please use 'next()' first.");
        }
        return currentPage.getSortVector();
    }

    /**
     * When no more items available.
     */
    @Override
    public void close() {
        active    = false;
        exhausted = true;
    }

}
