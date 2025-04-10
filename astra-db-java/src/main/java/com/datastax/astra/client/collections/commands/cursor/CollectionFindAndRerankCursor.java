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
import com.datastax.astra.client.collections.commands.options.CollectionFindAndRerankOptions;
import com.datastax.astra.client.core.paging.CursorState;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Projection;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.rerank.RerankedResult;
import com.datastax.astra.client.core.vector.DataAPIVector;
import com.datastax.astra.internal.command.AbstractCursor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Optional;


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
public class CollectionFindAndRerankCursor<T, R> extends AbstractCursor<T, RerankedResult<R>> {

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
    private final CollectionFindAndRerankOptions options;

    /**
     * Type of results (different from inputs when projection is used)
     */
    private Class<R> newRowType;

    /**
     * Cursor to iterate on the result of a query.
     *
     * @param dataSource
     *      source collection
     * @param filter
     *      current filter
     * @param options
     *      options of the find operation
     * @param recordType
     *      the type of the record
     */
    @SuppressWarnings("unchecked")
    public CollectionFindAndRerankCursor(Collection<T> dataSource, Filter filter, CollectionFindAndRerankOptions options, Class<R> recordType) {
        super((Class<RerankedResult<R>>) (Class<?>) RerankedResult.class);
        this.dataSource = dataSource;
        this.filter = filter;
        this.options = options;
        this.state  = CursorState.IDLE;
        this.buffer = new ArrayList<>();
        this.consumed = 0;
        this.newRowType = recordType;
    }

    /**
     * Constructor by copy. Once cloning the cursor is set back at the beginning.
     *
     * @param collectionFindCursor
     *      previous cursor
     */
    private CollectionFindAndRerankCursor(CollectionFindAndRerankCursor<T, R> collectionFindCursor) {
        super(collectionFindCursor.getRecordType());
        this.state                 = CursorState.IDLE;
        this.buffer                = new ArrayList<>();
        this.dataSource            = collectionFindCursor.dataSource;
        this.options               = collectionFindCursor.options;
        this.filter                = collectionFindCursor.filter;
        this.currentPage           = collectionFindCursor.currentPage;
        this.consumed              = collectionFindCursor.consumed;
        this.recordType            = collectionFindCursor.recordType;
    }

    /** {@inheritDoc} */
    @Override
    public CollectionFindAndRerankCursor<T, R> clone() {
        return new CollectionFindAndRerankCursor<>(this);
    }

    /**
     * Immutable methods that return a new Cursor instance.
     *
     * @param newFilter
     *      a new filter
     * @return
     *    a new cursor
     */
    public CollectionFindAndRerankCursor<T, R> filter(Filter newFilter) {
        checkIdleState();
        CollectionFindAndRerankCursor<T, R> newCursor = this.clone();
        newCursor.filter = newFilter;
        return newCursor;
    }

    /**
     * Creates a new {@link CollectionFindAndRerankCursor} with an updated projection.
     *
     * @param newProjection
     *      the new projection to apply
     * @return a new {@link CollectionFindAndRerankCursor} instance with the specified projection
     */
    public CollectionFindAndRerankCursor<T, R> project(Projection... newProjection) {
        checkIdleState();
        CollectionFindAndRerankCursor<T, R> newCursor = this.clone();
        newCursor.options.projection(newProjection);
        return newCursor;
    }

    /**
     * Creates a new {@link CollectionFindAndRerankCursor} with a specified sort order.
     *
     * @param sort
     *      the sort criteria to apply
     * @return a new {@link CollectionFindAndRerankCursor} instance with the specified sort order
     */
    public CollectionFindAndRerankCursor<T, R> sort(Sort... sort) {
        checkIdleState();
        CollectionFindAndRerankCursor<T, R> newCursor = this.clone();
        newCursor.options.sort(sort);
        return newCursor;
    }

    /**
     * Creates a new {@link CollectionFindAndRerankCursor} with a specified limit on the number of results.
     *
     * @param newLimit the maximum number of results to retrieve
     * @return a new {@link CollectionFindAndRerankCursor} instance with the specified limit
     */
    public CollectionFindAndRerankCursor<T, R> limit(int newLimit) {
        checkIdleState();
        CollectionFindAndRerankCursor<T, R> newCursor = this.clone();
        newCursor.limit(newLimit);
        return newCursor;
    }

    /**
     * Creates a new {@link CollectionFindAndRerankCursor} that includes similarity scores in the results.
     *
     * @return a new {@link CollectionFindAndRerankCursor} instance with similarity scores included
     */
    public CollectionFindAndRerankCursor<T, R> includeSimilarity() {
        checkIdleState();
        CollectionFindAndRerankCursor<T, R> newCursor = this.clone();
        newCursor.includeSimilarity();
        return newCursor;
    }

    /**
     * Creates a new {@link CollectionFindAndRerankCursor} that includes sort vector metadata in the results.
     *
     * @return a new {@link CollectionFindAndRerankCursor} instance with sort vector metadata included
     */
    public CollectionFindAndRerankCursor<T, R> includeSortVector() {
        checkIdleState();
        CollectionFindAndRerankCursor<T, R> newCursor = this.clone();
        newCursor.includeSortVector();
        return newCursor;
    }

    /**
     * {PageState is always null, all is returned here }
     */
    public void fetchNextBatch() {
        // Only the first time
        if (currentPage == null) {
            currentPage = dataSource.findAndRerankPage(filter, options, newRowType);
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