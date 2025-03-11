package com.datastax.astra.client.collections.commands.cursor;

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
import com.datastax.astra.client.collections.commands.options.CollectionFindOptions;
import com.datastax.astra.client.core.paging.CursorState;
import com.datastax.astra.client.core.paging.Page;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Projection;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.vector.DataAPIVector;
import com.datastax.astra.client.exceptions.CursorException;
import com.datastax.astra.client.tables.definition.rows.Row;
import com.datastax.astra.internal.serdes.tables.RowMapper;
import lombok.Getter;

import java.io.Closeable;
import java.util.*;
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
 * @param <DOC>
 *       working document object
 * @param <RES>
 *       working object for results, should be same as DOC if no projections
 */
public class CollectionFindCursor<DOC, RES> implements Iterable<RES>, Closeable, Cloneable {

    /**
     * Input table reference
     */
    @Getter
    private final Collection<DOC> collection;

    /**
     * Input Filter provided.
     * Immutable as not setter is provided.
     */
    private Filter filter;

    /**
     * Input Find options. Where will change the different options.
     * Immutable as not setter is provided.
     */
    private CollectionFindOptions collectionFindOptions;

    /**
     * Cursor state.
     */
    private CursorState state;

    /**
     * Records to process
     */
    private List<DOC> buffer;

    /**
     * Current page
     */
    private Page<DOC> currentPage;

    /**
     * How many consumed in the current buffer.
     */
    @Getter
    private int consumedCount;

    /**
     * Type of the row to return
     */
    @Getter
    private Class<RES> documentType;

    /**
     * Cursor to iterate on the result of a query.
     *
     * @param collection
     *      source collection
     * @param filter
     *      current filter
     * @param options
     *      options of the find operation
     * @param rowType
     *      row type returned with the cursor
     */
    public CollectionFindCursor(Collection<DOC> collection, Filter filter, CollectionFindOptions options, Class<RES> rowType) {
        this.collection = collection;
        this.filter = filter;
        this.documentType = rowType;
        this.collectionFindOptions = options;
        this.state  = CursorState.IDLE;
        this.buffer = new ArrayList<>();
        this.consumedCount = 0;
    }

    /**
     * Constructor by copy. Once cloning the cursor is set back at the beginning.
     *
     * @param collectionFindCursor
     *      previous cursor
     */
    private CollectionFindCursor(CollectionFindCursor<DOC, RES> collectionFindCursor) {
        if (collectionFindCursor == null) {
            throw new IllegalArgumentException("Input cursor should not be null");
        }
        this.state = CursorState.IDLE;
        this.buffer                = new ArrayList<>();
        this.collection            = collectionFindCursor.collection;
        this.collectionFindOptions = collectionFindCursor.collectionFindOptions;
        this.filter                = collectionFindCursor.filter;
        this.currentPage           = collectionFindCursor.currentPage;
        this.documentType          = collectionFindCursor.documentType;
        this.consumedCount         = collectionFindCursor.consumedCount;
    }

    /** {@inheritDoc} */
    @Override
    public CollectionFindCursor<DOC, RES> clone() {
        return new CollectionFindCursor<>(this);
    }

    /**
     * Immutable methods that return a new Cursor instance.
     *
     * @param newFilter
     *      a new filter
     * @return
     *    a new cursor
     */
    public CollectionFindCursor<DOC, RES> filter(Filter newFilter) {
        checkIdleState();
        CollectionFindCursor<DOC, RES> newCursor = this.clone();
        newCursor.filter = newFilter;
        return newCursor;
    }

    /**
     * Creates a new {@link CollectionFindCursor} with an updated projection.
     *
     * @param newProjection the new projection to apply
     * @return a new {@link CollectionFindCursor} instance with the specified projection
     */
    public CollectionFindCursor<DOC, RES> project(Projection... newProjection) {
        checkIdleState();
        CollectionFindCursor<DOC, RES> newCursor = this.clone();
        newCursor.collectionFindOptions.projection(newProjection);
        return newCursor;
    }

    /**
     * Creates a new {@link CollectionFindCursor} with a specified sort order.
     *
     * @param sort the sort criteria to apply
     * @return a new {@link CollectionFindCursor} instance with the specified sort order
     */
    public CollectionFindCursor<DOC, RES> sort(Sort... sort) {
        checkIdleState();
        CollectionFindCursor<DOC, RES> newCursor = this.clone();
        newCursor.collectionFindOptions.sort(sort);
        return newCursor;
    }

    /**
     * Creates a new {@link CollectionFindCursor} with a specified limit on the number of results.
     *
     * @param newLimit the maximum number of results to retrieve
     * @return a new {@link CollectionFindCursor} instance with the specified limit
     */
    public CollectionFindCursor<DOC, RES> limit(int newLimit) {
        checkIdleState();
        CollectionFindCursor<DOC, RES> newCursor = this.clone();
        newCursor.limit(newLimit);
        return newCursor;
    }

    /**
     * Creates a new {@link CollectionFindCursor} that skips a specified number of results.
     *
     * @param newSkip the number of results to skip
     * @return a new {@link CollectionFindCursor} instance with the specified skip value
     */
    public CollectionFindCursor<DOC, RES> skip(int newSkip) {
        checkIdleState();
        CollectionFindCursor<DOC, RES> newCursor = this.clone();
        newCursor.skip(newSkip);
        return newCursor;
    }

    /**
     * Creates a new {@link CollectionFindCursor} that includes similarity scores in the results.
     *
     * @return a new {@link CollectionFindCursor} instance with similarity scores included
     */
    public CollectionFindCursor<DOC, RES> includeSimilarity() {
        checkIdleState();
        CollectionFindCursor<DOC, RES> newCursor = this.clone();
        newCursor.includeSimilarity();
        return newCursor;
    }

    /**
     * Creates a new {@link CollectionFindCursor} that includes sort vector metadata in the results.
     *
     * @return a new {@link CollectionFindCursor} instance with sort vector metadata included
     */
    public CollectionFindCursor<DOC, RES> includeSortVector() {
        checkIdleState();
        CollectionFindCursor<DOC, RES> newCursor = this.clone();
        newCursor.includeSortVector();
        return newCursor;
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
    public List<DOC> consumeBuffer(int n) {
        if (state == CursorState.CLOSED || state == CursorState.IDLE) {
            return Collections.emptyList();
        }
        List<DOC> result = new ArrayList<>();
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
    public Iterator<RES> iterator() {
        return new CursorIterator();
    }

    /**
     * Fetches the next batch of documents into the buffer.
     * This method handles paging, using the page state from the previous batch to fetch the next one.
     */
    private void fetchNextBatch() {
        if (currentPage == null) {
            // Searching First Page
            currentPage = collection.findPage(filter, collectionFindOptions);
            buffer.addAll(currentPage.getResults());
        } else if (currentPage.getPageState().isPresent()) {
            // Searching next page if exist
            collectionFindOptions.pageState(currentPage.getPageState().get());
            currentPage = collection.findPage(filter, collectionFindOptions);
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
     * @throws java.util.NoSuchElementException if no more elements are available
     */
    public RES next() {
        return iterator().next();
    }

    /**
     * Collects all remaining elements in the cursor into a list.
     * Automatically closes the cursor after all elements are consumed.
     *
     * @return a {@link List} containing all remaining elements
     */
    public List<RES> toList() {
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
    public Stream<RES> stream() {
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
        return collection.getKeyspaceName();
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
     * A private iterator implementation for iterating over the results of a {@link CollectionFindCursor}.
     * Handles lazy loading of data in batches to optimize memory usage and performance.
     */
    private class CursorIterator implements Iterator<RES> {

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
        @SuppressWarnings("unchecked")
        public RES next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            DOC rawDoc = buffer.remove(0);
            consumedCount++;

            if (!documentType.isInstance(rawDoc)) {
                Row row = RowMapper.mapAsRow(rawDoc);
                return RowMapper.mapFromRow(row, collection.getOptions().getSerializer(), documentType);
            } else {
                return (RES) rawDoc;
            }
        }
    }

}