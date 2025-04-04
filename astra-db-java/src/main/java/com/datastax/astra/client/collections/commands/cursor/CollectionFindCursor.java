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
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.paging.CursorState;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Projection;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.tables.definition.rows.Row;
import com.datastax.astra.internal.command.AbstractCursor;
import com.datastax.astra.internal.serdes.tables.RowMapper;
import lombok.Getter;

import java.util.*;


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
 *       working document object
 * @param <R>
 *       working object for results, should be same as DOC if no projections
 */
public class CollectionFindCursor<T, R> extends AbstractCursor<T, R> {

    /**
     * Input table reference
     */
    @Getter
    private final Collection<T> dataSource;

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
     * Cursor to iterate on the result of a query.
     *
     * @param dataSource
     *      source collection
     * @param filter
     *      current filter
     * @param options
     *      options of the find operation
     * @param documentType
     *      row type returned with the cursor
     */
    public CollectionFindCursor(Collection<T> dataSource, Filter filter, CollectionFindOptions options, Class<R> documentType) {
        super(documentType);
        this.dataSource = dataSource;
        this.filter = filter;
        this.collectionFindOptions = options;
        this.state  = CursorState.IDLE;
        this.buffer = new ArrayList<>();
        this.consumed = 0;
    }

    /**
     * Constructor by copy. Once cloning the cursor is set back at the beginning.
     *
     * @param collectionFindCursor
     *      previous cursor
     */
    private CollectionFindCursor(CollectionFindCursor<T, R> collectionFindCursor) {
        super(collectionFindCursor.getRecordType());
        this.state = CursorState.IDLE;
        this.buffer                = new ArrayList<>();
        this.dataSource = collectionFindCursor.dataSource;
        this.collectionFindOptions = collectionFindCursor.collectionFindOptions;
        this.filter                = collectionFindCursor.filter;
        this.currentPage           = collectionFindCursor.currentPage;
        this.consumed              = collectionFindCursor.consumed;
        this.recordType            = collectionFindCursor.recordType;
    }

    /** {@inheritDoc} */
    @Override
    public CollectionFindCursor<T, R> clone() {
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
    public CollectionFindCursor<T, R> filter(Filter newFilter) {
        checkIdleState();
        CollectionFindCursor<T, R> newCursor = this.clone();
        newCursor.filter = newFilter;
        return newCursor;
    }

    /**
     * Creates a new {@link CollectionFindCursor} with an updated projection.
     *
     * @param newProjection the new projection to apply
     * @return a new {@link CollectionFindCursor} instance with the specified projection
     */
    public CollectionFindCursor<T, R> project(Projection... newProjection) {
        checkIdleState();
        CollectionFindCursor<T, R> newCursor = this.clone();
        newCursor.collectionFindOptions.projection(newProjection);
        return newCursor;
    }

    /**
     * Creates a new {@link CollectionFindCursor} with a specified sort order.
     *
     * @param sort the sort criteria to apply
     * @return a new {@link CollectionFindCursor} instance with the specified sort order
     */
    public CollectionFindCursor<T, R> sort(Sort... sort) {
        checkIdleState();
        CollectionFindCursor<T, R> newCursor = this.clone();
        newCursor.collectionFindOptions.sort(sort);
        return newCursor;
    }

    /**
     * Creates a new {@link CollectionFindCursor} with a specified limit on the number of results.
     *
     * @param newLimit the maximum number of results to retrieve
     * @return a new {@link CollectionFindCursor} instance with the specified limit
     */
    public CollectionFindCursor<T, R> limit(int newLimit) {
        checkIdleState();
        CollectionFindCursor<T, R> newCursor = this.clone();
        newCursor.limit(newLimit);
        return newCursor;
    }

    /**
     * Creates a new {@link CollectionFindCursor} that skips a specified number of results.
     *
     * @param newSkip the number of results to skip
     * @return a new {@link CollectionFindCursor} instance with the specified skip value
     */
    public CollectionFindCursor<T, R> skip(int newSkip) {
        checkIdleState();
        CollectionFindCursor<T, R> newCursor = this.clone();
        newCursor.skip(newSkip);
        return newCursor;
    }

    /**
     * Creates a new {@link CollectionFindCursor} that includes similarity scores in the results.
     *
     * @return a new {@link CollectionFindCursor} instance with similarity scores included
     */
    public CollectionFindCursor<T, R> includeSimilarity() {
        checkIdleState();
        CollectionFindCursor<T, R> newCursor = this.clone();
        newCursor.includeSimilarity();
        return newCursor;
    }

    /**
     * Creates a new {@link CollectionFindCursor} that includes sort vector metadata in the results.
     *
     * @return a new {@link CollectionFindCursor} instance with sort vector metadata included
     */
    public CollectionFindCursor<T, R> includeSortVector() {
        checkIdleState();
        CollectionFindCursor<T, R> newCursor = this.clone();
        newCursor.includeSortVector();
        return newCursor;
    }

    /**
     * Fetches the next batch of documents into the buffer.
     * This method handles paging, using the page state from the previous batch to fetch the next one.
     */
    public void fetchNextBatch() {
        if (currentPage == null) {
            // Searching First Page
            currentPage = dataSource.findPage(filter, collectionFindOptions, getRecordType());
            buffer.addAll(currentPage.getResults());
        } else if (currentPage.getPageState().isPresent()) {
            // Searching next page if exist
            collectionFindOptions.pageState(currentPage.getPageState().get());
            currentPage = dataSource.findPage(filter, collectionFindOptions, getRecordType());
            buffer.addAll(currentPage.getResults());
        }
    }

    /**
     * Retrieve keyspace name.
     *
     * @return
     *      keyspace name
     */
    public String getKeyspace() {
        return dataSource.getKeyspaceName();
    }
}