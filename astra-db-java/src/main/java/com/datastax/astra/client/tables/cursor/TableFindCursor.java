package com.datastax.astra.client.tables.cursor;

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
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Projection;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.vector.DataAPIVector;
import com.datastax.astra.client.exceptions.CursorException;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.commands.options.TableFindOptions;
import lombok.Getter;

import java.io.Closeable;
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
/**
 * Implementation of a cursor across the find items
 *
 * @param <T>
 *       working bean of parent table
 * @param <R>
 *       working bean returned for the find
 */
public class TableFindCursor<T, R> implements Iterable<R>, Closeable, Cloneable {

    /**
     * Input table reference
     */
    @Getter
    private final Table<T> table;

    /**
     * Input Filter provided.
     * Immutable as not setter is provided.
     */
    private Filter filter;

    /**
     * Input Find options. Where will change the different options.
     * Immutable as not setter is provided.
     */
    private TableFindOptions tableFindOptions;

    /**
     * Cursor state.
     */
    private CursorState state;

    /**
     * Records to process
     */
    private List<R> buffer;

    /**
     * Current page
     */
    private Page<R> currentPage;

    /**
     * How many consumed in the current buffer.
     */
    @Getter
    private int consumedCount;

    /**
     * Type of the row to return
     */
    @Getter
    private Class<R> rowType;

    /**
     * Cursor to iterate on the result of a query.
     *
     * @param table
     *      source table
     * @param filter
     *      current filter
     * @param options
     *      options of the find operation
     * @param rowType
     *      row type returned with the cursor
     */
    public TableFindCursor(Table<T> table, Filter filter, TableFindOptions options, Class<R> rowType) {
        this.table = table;
        this.filter = filter;
        this.rowType = rowType;
        this.tableFindOptions = options;
        this.state = CursorState.IDLE;
        this.buffer = new ArrayList<>();
        this.consumedCount = 0;
    }

    /**
     * Constructor by copy. Once cloning the cursor is set back at the beginning.
     *
     * @param tableFindCursor
     *      previous cursor
     */
    private TableFindCursor(TableFindCursor<T, R> tableFindCursor) {
        this.state = CursorState.IDLE;
        this.table = tableFindCursor.table;
        this.tableFindOptions = tableFindCursor.tableFindOptions;
        this.filter           = tableFindCursor.filter;
        this.buffer           = new ArrayList<>();
        this.consumedCount    = 0;
        this.currentPage      = tableFindCursor.currentPage;
        this.rowType         = tableFindCursor.rowType;
    }

    /** {@inheritDoc} */
    @Override
    public TableFindCursor<T, R> clone() {
        return new TableFindCursor<>(this);
    }

    /**
     * Immutable methods that return a new Cursor instance.
     *
     * @param newFilter
     *      a new filter
     * @return
     *    a new cursor
     */
    public TableFindCursor<T, R> filter(Filter newFilter) {
        checkIdleState();
        TableFindCursor<T, R> newTableFindCursor = this.clone();
        newTableFindCursor.filter = newFilter;
        return newTableFindCursor;
    }

    /**
     * Creates a new {@link TableFindCursor} with an updated projection.
     *
     * @param newProjection the new projection to apply
     * @return a new {@link TableFindCursor} instance with the specified projection
     */
    public TableFindCursor<T, R> project(Projection... newProjection) {
        checkIdleState();
        TableFindCursor<T, R> newTableFindCursor = this.clone();
        newTableFindCursor.tableFindOptions.projection(newProjection);
        return newTableFindCursor;
    }

    /**
     * Creates a new {@link TableFindCursor} with a specified sort order.
     *
     * @param sort the sort criteria to apply
     * @return a new {@link TableFindCursor} instance with the specified sort order
     */
    public TableFindCursor<T, R> sort(Sort... sort) {
        checkIdleState();
        TableFindCursor<T, R> newTableFindCursor = this.clone();
        newTableFindCursor.tableFindOptions.sort(sort);
        return newTableFindCursor;
    }

    /**
     * Creates a new {@link TableFindCursor} with a specified limit on the number of results.
     *
     * @param newLimit the maximum number of results to retrieve
     * @return a new {@link TableFindCursor} instance with the specified limit
     */
    public TableFindCursor<T, R> limit(int newLimit) {
        checkIdleState();
        TableFindCursor<T, R> newTableFindCursor = this.clone();
        newTableFindCursor.limit(newLimit);
        return newTableFindCursor;
    }

    /**
     * Creates a new {@link TableFindCursor} that skips a specified number of results.
     *
     * @param newSkip the number of results to skip
     * @return a new {@link TableFindCursor} instance with the specified skip value
     */
    public TableFindCursor<T, R> skip(int newSkip) {
        checkIdleState();
        TableFindCursor<T, R> newTableFindCursor = this.clone();
        newTableFindCursor.skip(newSkip);
        return newTableFindCursor;
    }

    /**
     * Creates a new {@link TableFindCursor} that includes similarity scores in the results.
     *
     * @return a new {@link TableFindCursor} instance with similarity scores included
     */
    public TableFindCursor<T, R> includeSimilarity() {
        checkIdleState();
        TableFindCursor<T, R> newTableFindCursor = this.clone();
        newTableFindCursor.includeSimilarity();
        return newTableFindCursor;
    }

    /**
     * Creates a new {@link TableFindCursor} that includes sort vector metadata in the results.
     *
     * @return a new {@link TableFindCursor} instance with sort vector metadata included
     */
    public TableFindCursor<T, R> includeSortVector() {
        checkIdleState();
        TableFindCursor<T, R> newTableFindCursor = this.clone();
        newTableFindCursor.includeSortVector();
        return newTableFindCursor;
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
    private void checkIdleState() {
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
    private void fetchNextBatch() {
        if (currentPage == null) {
            // Searching First Page
            currentPage = table.findPage(filter, tableFindOptions, rowType);
            buffer.addAll(currentPage.getResults());
        } else if (currentPage.getPageState().isPresent()) {
            // Searching next page if exist
            tableFindOptions.pageState(currentPage.getPageState().get());
            currentPage = table.findPage(filter, tableFindOptions, rowType);
            buffer.addAll(currentPage.getResults());
        }
    }

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
    /**
     * Collects all remaining elements in the cursor into a list.
     * Automatically closes the cursor after all elements are consumed.
     *
     * @return a {@link List} containing all remaining elements
     */
    public List<R> toList() {
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
        return table.getKeyspaceName();
    }

    /**
     * Access to the Sort Vector.
     *
     * @return
     *      sort vector
     */
    public Optional<DataAPIVector> getSortVector() {
        if (currentPage == null && state == CursorState.IDLE) {
            fetchNextBatch();
        }
        if (currentPage == null) {
            return Optional.empty();
        }
        return currentPage.getSortVector();
    }

    /**
     * A private iterator implementation for iterating over the results of a {@link TableFindCursor}.
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
            fetchNextBatch();
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
            consumedCount++;
            return rawDoc;
        }
    }

}