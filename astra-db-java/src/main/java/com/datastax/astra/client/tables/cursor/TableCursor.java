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
import com.datastax.astra.client.tables.definition.rows.Row;
import com.datastax.astra.internal.serdes.tables.RowMapper;
import lombok.Getter;

import java.io.Closeable;
import java.util.*;

/**
 * Implementation of a cursor across the find items
 *
 * @param <T>
 *       working bean of parent table
 * @param <R>
 *       working bean returned for the find
 */
public class TableCursor<T, R> implements Iterable<R>, Closeable, Cloneable {

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
     * Type of the row to return
     */
    @Getter
    private Class<R> rowType;

    /** Allow to return only distincts values; */
    private boolean distinct = false;

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
    public TableCursor(Table<T> table, Filter filter, TableFindOptions options, boolean distinct, Class<R> rowType) {
        this.table = table;
        this.filter = filter;
        this.rowType = rowType;
        this.tableFindOptions = options;
        this.state = CursorState.IDLE;
        this.buffer = new ArrayList<>();
        this.consumedCount = 0;
        this.distinct = distinct;
    }

    /**
     * Constructor by copy. Once cloning the cursor is set back at the beginning.
     *
     * @param tableCursor
     *      previous cursor
     */
    private TableCursor(TableCursor<T, R> tableCursor) {
        if (tableCursor == null) {
            throw new IllegalArgumentException("Input cursor cannot be null");
        }
        this.table            = tableCursor.table;
        this.tableFindOptions = tableCursor.tableFindOptions;
        this.filter           = tableCursor.filter;
        this.currentPage      = tableCursor.currentPage;
        this.rowType          = tableCursor.rowType;
        this.distinct         = tableCursor.distinct;
        this.state            = CursorState.IDLE;
        this.buffer           = new ArrayList<>();
        this.consumedCount    = 0;
    }

    /** {@inheritDoc} */
    @Override
    public TableCursor<T, R> clone() {
        return new TableCursor<>(this);
    }

    /**
     * Immutable methods that return a new Cursor instance.
     *
     * @param newFilter
     *      a new filter
     * @return
     *    a new cursor
     */
    public TableCursor<T, R> filter(Filter newFilter) {
        checkIdleState();
        TableCursor<T, R> newTableCursor = this.clone();
        newTableCursor.filter = newFilter;
        return newTableCursor;
    }

    /**
     * Creates a new {@link TableCursor} with an updated projection.
     *
     * @param newProjection the new projection to apply
     * @return a new {@link TableCursor} instance with the specified projection
     */
    public TableCursor<T, R> project(Projection... newProjection) {
        checkIdleState();
        TableCursor<T, R> newTableCursor = this.clone();
        newTableCursor.tableFindOptions.projection(newProjection);
        return newTableCursor;
    }

    /**
     * Creates a new {@link TableCursor} with a specified sort order.
     *
     * @param sort the sort criteria to apply
     * @return a new {@link TableCursor} instance with the specified sort order
     */
    public TableCursor<T, R> sort(Sort... sort) {
        checkIdleState();
        TableCursor<T, R> newTableCursor = this.clone();
        newTableCursor.tableFindOptions.sort(sort);
        return newTableCursor;
    }

    /**
     * Creates a new {@link TableCursor} with a specified limit on the number of results.
     *
     * @param newLimit the maximum number of results to retrieve
     * @return a new {@link TableCursor} instance with the specified limit
     */
    public TableCursor<T, R> limit(int newLimit) {
        checkIdleState();
        TableCursor<T, R> newTableCursor = this.clone();
        newTableCursor.limit(newLimit);
        return newTableCursor;
    }

    /**
     * Creates a new {@link TableCursor} that skips a specified number of results.
     *
     * @param newSkip the number of results to skip
     * @return a new {@link TableCursor} instance with the specified skip value
     */
    public TableCursor<T, R> skip(int newSkip) {
        checkIdleState();
        TableCursor<T, R> newTableCursor = this.clone();
        newTableCursor.skip(newSkip);
        return newTableCursor;
    }

    /**
     * Creates a new {@link TableCursor} that includes similarity scores in the results.
     *
     * @return a new {@link TableCursor} instance with similarity scores included
     */
    public TableCursor<T, R> includeSimilarity() {
        checkIdleState();
        TableCursor<T, R> newTableCursor = this.clone();
        newTableCursor.includeSimilarity();
        return newTableCursor;
    }

    /**
     * Creates a new {@link TableCursor} that includes sort vector metadata in the results.
     *
     * @return a new {@link TableCursor} instance with sort vector metadata included
     */
    public TableCursor<T, R> includeSortVector() {
        checkIdleState();
        TableCursor<T, R> newTableCursor = this.clone();
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

    /**
     * Consume the buffer and return the list of items.
     *
     * @param n
     *      number of items to consume
     * @return
     *      list of items
     */
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
            currentPage = table.findPage(filter, tableFindOptions);
            buffer.addAll(currentPage.getResults());
        } else if (currentPage.getPageState().isPresent()) {
            // Searching next page if exist
            tableFindOptions.pageState(currentPage.getPageState().get());
            currentPage = table.findPage(filter, tableFindOptions);
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
    public List<R> toList() {
        List<R> result = new ArrayList<>();
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
        return table.getKeyspaceName();
    }

    /**
     * Access to the Sort Vector.
     *
     * @return
     *      sort vector
     */
    public Optional<DataAPIVector> getSortVector() {
        if (currentPage == null) {
            return Optional.empty();
        }
        return currentPage.getSortVector().map(DataAPIVector::new);
    }


    /**
     * A private iterator implementation for iterating over the results of a {@link TableCursor}.
     * Handles lazy loading of data in batches to optimize memory usage and performance.
     */
    private class CursorIterator implements Iterator<R> {

        /**
         * Store distinct values returned.
         */
        private final Set<R> returnedValues = distinct ? new HashSet<>() : null;

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

            // Keep fetching the next batch if the buffer is empty
            while (buffer.isEmpty() && currentPage != null && currentPage.getPageState().isPresent()) {
                fetchNextBatch();
            }

            // Ensure we have elements and check distinct constraint
            while (!buffer.isEmpty()) {
                R nextValue = peekNext();
                if (!distinct || returnedValues.add(nextValue)) {
                    return true;
                } else {
                    buffer.remove(0); // Remove duplicate
                }
            }
            return false;
        }

        /**
         * Retrieves the next element in the iteration.
         *
         * @return the next element of type {@code R}
         * @throws NoSuchElementException if no more elements are available
         */
        @Override
        @SuppressWarnings("unchecked")
        public R next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            T rawDoc = buffer.remove(0);
            consumedCount++;

            R result;
            if (!rowType.isInstance(rawDoc)) {
                Row row = RowMapper.mapAsRow(rawDoc);
                result = RowMapper.mapFromRow(row, table.getOptions().getSerializer(), rowType);
            } else {
                result = (R) rawDoc;
            }

            if (distinct) {
                returnedValues.add(result);
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        private R peekNext() {
            T rawDoc = buffer.get(0);
            if (!rowType.isInstance(rawDoc)) {
                Row row = RowMapper.mapAsRow(rawDoc);
                return RowMapper.mapFromRow(row, table.getOptions().getSerializer(), rowType);
            } else {
                return (R) rawDoc;
            }
        }


    }

}
