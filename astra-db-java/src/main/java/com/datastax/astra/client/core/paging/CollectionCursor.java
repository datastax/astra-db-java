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
import com.datastax.astra.client.core.query.Projection;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.exception.CursorException;
import lombok.Getter;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Implementation of a cursor across the find items
 *
 * @param <T>
 *       type of the table
 */
public class CollectionCursor<T> implements Iterable<T>, Closeable, Cloneable {

    /**
     * Input table reference
     */
    @Getter
    private final Collection<T> myCollection;

    /**
     * Input Filter provided.
     * Immutable as not setter is provided.
     */
    private Filter filter;

    /**
     * Input Find options. Where will change the different options.
     * Immutable as not setter is provided.
     */
    private CollectionFindOptions findOptions;

    /**
     * Cursor state.
     */
    private CursorState state;

    /**
     * Records to process
     */
    private List<T> buffer;

    /**
     * Current page
     */
    private Page<T> currentPage;

    /**
     * How many consumed in the current buffer.
     */
    @Getter
    private int consumedCount;

    /**
     * Cursor to iterate on the result of a query.
     *
     * @param col
     *      source collection
     * @param filter
     *      current filter
     * @param options
     *      options of the find operation
     */
    public CollectionCursor(Collection<T> col, Filter filter, CollectionFindOptions options) {
        this.myCollection = col;
        this.filter = filter;
        this.findOptions = options;
        this.state = CursorState.IDLE;
        this.buffer = new ArrayList<>();
        this.consumedCount = 0;
    }

    /**
     * Constructor by copy. Once cloning the cursor is set back at the beginning.
     *
     * @param colCursor
     *      previous cursor
     */
    private CollectionCursor(CollectionCursor<T> colCursor) {
        this.state = CursorState.IDLE;
        this.myCollection = colCursor.myCollection;
        this.findOptions = colCursor.findOptions;
        this.filter = colCursor.filter;
        this.buffer = new ArrayList<>();
        this.consumedCount = 0;
    }

    /** {@inheritDoc} */
    @Override
    public CollectionCursor<T> clone() {
        return new CollectionCursor<>(this);
    }

    /**
     * Immutable methods that return a new Cursor instance.
     *
     * @param newFilter
     *      a new filter
     */
    public CollectionCursor<T> filter(Filter newFilter) {
        checkIdleState();
        CollectionCursor<T> newTableCursor = this.clone();
        newTableCursor.filter = newFilter;
        return newTableCursor;
    }

    /**
     * Immutable methods that return a new Cursor instance.
     *
     * @param newProjection
     *      a new projection
     */
    public CollectionCursor<T> project(Projection... newProjection) {
        checkIdleState();
        CollectionCursor<T> newTableCursor = this.clone();
        newTableCursor.findOptions.projection(newProjection);
        return newTableCursor;
    }

    public CollectionCursor<T> sort(Sort... sort) {
        checkIdleState();
        CollectionCursor<T> newTableCursor = this.clone();
        newTableCursor.findOptions.sort(sort);
        return newTableCursor;
    }

    public CollectionCursor<T> limit(int newLimit) {
        checkIdleState();
        CollectionCursor<T> newTableCursor = this.clone();
        newTableCursor.limit(newLimit);
        return newTableCursor;
    }

    public CollectionCursor<T> skip(int newSkip) {
        checkIdleState();
        CollectionCursor<T> newTableCursor = this.clone();
        newTableCursor.skip(newSkip);
        return newTableCursor;
    }

    public CollectionCursor<T> includeSimilarity() {
        checkIdleState();
        CollectionCursor<T> newTableCursor = this.clone();
        newTableCursor.includeSimilarity();
        return newTableCursor;
    }

    public CollectionCursor<T> includeSortVector() {
        checkIdleState();
        CollectionCursor<T> newTableCursor = this.clone();
        newTableCursor.includeSortVector();
        return newTableCursor;
    }

    /**
     * Change the state of the cursor to close.
     */
    @Override
    public void close() {
        this.state = CursorState.CLOSED;
    }

    /**
     * Rewind the cursor to the beginning.
     */
    private void rewind() {
        this.state = CursorState.IDLE;
        this.buffer.clear();
        this.consumedCount = 0;
    }

    // Buffer consumption
    public List<T> consumeBuffer(int n) {
        if (state == CursorState.CLOSED || state == CursorState.IDLE) {
            return Collections.emptyList();
        }
        List<T> result = new ArrayList<>();
        int count = 0;
        while (!buffer.isEmpty() && count < n) {
            result.add(buffer.remove(0));
            count++;
        }
        return result;
    }

    /**
     * Validate that the cursor is in the IDLE state.
     */
    private void checkIdleState() {
        if (state != CursorState.IDLE) {
            throw new CursorException("Cannot modify cursor after it has been started.", state.toString());
        }
    }

    // Iterator implementation
    @Override
    public Iterator<T> iterator() {
        return new CursorIterator();
    }

    /**
     * Iterator about options
     */
    private class CursorIterator implements Iterator<T> {

        @Override
        public boolean hasNext() {
            if (state == CursorState.CLOSED) {
                return false;
            }
            if (state == CursorState.IDLE) {
                state = CursorState.STARTED;
            }
            if (!buffer.isEmpty()) {
                return true;
            }
            // Fetch next batch of documents into buffer (if buffer is empty)
            fetchNextBatch();
            return !buffer.isEmpty();
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            T rawDoc = buffer.remove(0);
            consumedCount++;
            return (T) rawDoc;
        }
    }

    // Fetch next batch of documents
    private void fetchNextBatch() {
        if (currentPage == null) {
            currentPage = myCollection.findPage(filter, findOptions);
            buffer.addAll(currentPage.getResults());
        } else if (currentPage.getPageState().isPresent()) {
            findOptions.pageState(currentPage.getPageState().get());
            currentPage = myCollection.findPage(filter, findOptions);
            buffer.addAll(currentPage.getResults());
        } else {
            System.out.println("no");
        }
    }

    // Additional methods
    public boolean hasNext() {
        return iterator().hasNext();
    }

    public T next() {
        return iterator().next();
    }

    public List<T> toList() {
        List<T> result = new ArrayList<>();
        try {
            forEach(result::add);
        } finally {
            close();
        }
        return result;
    }

    /**
     * Access the size of the buffer.
     *
     * @return
     *      buffer count
     */
    public int getBufferedCount() {
        return buffer.size();
    }

    /**
     * Retrieve keyspace name.
     *
     * @return
     *      keyspace name
     */
    public String getKeyspace() {
        return myCollection.getKeyspaceName();
    }

}
