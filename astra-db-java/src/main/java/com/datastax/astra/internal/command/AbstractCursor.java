package com.datastax.astra.internal.command;

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

import com.datastax.astra.client.core.paging.CursorState;
import com.datastax.astra.client.core.paging.Page;
import com.datastax.astra.client.core.vector.DataAPIVector;
import com.datastax.astra.client.exceptions.CursorException;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Implementation of a cursor across the find items
 *
 * @param <T>
 *       working bean of parent table
 * @param <R>
 *       working bean returned for the find
 */
public abstract class AbstractCursor<T, R> implements Iterable<R>, Cloneable {

    /**
     * Cursor state.
     */
    @Getter
    protected CursorState state = CursorState.IDLE;

    /**
     * Records to process
     */
    @Getter
    protected List<R> buffer;

    /**
     * Current page
     */
    protected Page<R> currentPage;

    /**
     * How many consumed in the current buffer.
     */
    @Getter
    protected int consumed;

    /**
     * Type of the row to return
     */
    @Getter
    protected Class<R> recordType;

    /**
     * Cursor to iterate on the result of a query.
     *
     * @param recordType
     *      row type returned with the cursor
     */
    protected AbstractCursor(Class<R> recordType) {
        this.recordType = recordType;
        this.buffer = new ArrayList<>();
        this.consumed = 0;
    }

    /**
     * Change the state of the cursor to close.
     */
    public void close() {
        this.state = CursorState.CLOSED;
    }

    /**
     * Rewind the cursor to the beginning.
     */
    public void rewind() {
        this.state = CursorState.IDLE;
        this.buffer.clear();
        this.currentPage = null;
        this.consumed = 0;
    }

    /**
     * Consume the buffer and return the list of items.
     *
     * @param n
     *      number of items to consume
     * @return
     *      list of items
     */
    public List<R> consumeBuffer(int n) {
        if (state == CursorState.CLOSED || state == CursorState.IDLE) {
            return Collections.emptyList();
        }
        List<R> result = new ArrayList<>();
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
    protected void checkIdleState() {
        if (state != CursorState.IDLE) {
            throw new CursorException("Cannot modify cursor after it has been started.", state.toString());
        }
    }

    /**
     * Iterate over the cursor.
     *
     * @return
     *     iterator over the results
     */
    @Override
    public Iterator<R> iterator() {
        return new CursorIterator();
    }

    /**
     * Fetches the next batch of documents into the buffer.
     * This method handles paging, using the page state from the previous batch to fetch the next one.
     */
    protected abstract void fetchNextPage();

    /**
     * Checks if there are more elements in the cursor.
     *
     * @return {@code true} if there are more elements, {@code false} otherwise
     */
    public boolean hasNext() {
        return iterator().hasNext();
    }

    /**
     * Retrieves the next element from the cursor.
     *
     * @return the next element of type {@code R}
     * @throws NoSuchElementException if no more elements are available
     */
    public R next() {
        return iterator().next();
    }

    /**
     * Collects all remaining elements in the cursor into a list.
     * Automatically closes the cursor after all elements are consumed.
     *
     * @return a {@link List} containing all remaining elements
     */
    public List<R> toList() {
        // Should return an illegal state is the cursor is closed
        if (state == CursorState.CLOSED) {
            throw new CursorException("Cursor is closed", state.toString());
        }
        if (state == CursorState.STARTED) {
            throw new CursorException("Cursor is already started", state.toString());
        }
        try {
            return stream().toList();
        } finally {
            close();
        }
    }

    /**
     * Convert the current cursor as a stream
     *
     * @return
     *      current as a stream
     */
    public Stream<R> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    /**
     * Access the size of the buffer.
     *
     * @return
     *      buffer count
     */
    public int getBufferedSize() {
        return buffer.size();
    }

    /**
     * Access to the Sort Vector.
     *
     * @return
     *      sort vector
     */
    public Optional<DataAPIVector> getSortVector() {
        if (currentPage == null && state == CursorState.IDLE) {
            fetchNextPage();
        }
        if (currentPage == null) {
            return Optional.empty();
        }
        return currentPage.getSortVector();
    }

    /**
     * A private iterator implementation for iterating over the results of a {@link AbstractCursor}.
     * Handles lazy loading of data in batches to optimize memory usage and performance.
     */
    private class CursorIterator implements Iterator<R> {

        /**
         * Checks if there are more elements to iterate over.
         * If the buffer is empty, it fetches the next batch of documents.
         *
         * @return {@code true} if there are more elements, {@code false} otherwise
         */
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
            fetchNextPage();
            return !buffer.isEmpty();
        }

        /**
         * Retrieves the next element in the iteration.
         *
         * @return the next element of type {@code R}
         * @throws NoSuchElementException if no more elements are available
         */
        @Override
        public R next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            R rawDoc = buffer.remove(0);
            consumed++;
            return rawDoc;
        }
    }

}