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
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Projection;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.vector.DataAPIVector;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.commands.options.TableFindOptions;
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
 *       working bean of parent table
 * @param <R>
 *       working bean returned for the find
 */
public class TableFindCursor<T, R> extends AbstractCursor<T, R> {

    /**
     * Input table reference
     */
    @Getter
    private final Table<T> dataSource;

    /**
     * Input Filter provided.
     * Immutable as not setter is provided.
     */
    private Filter filter;

    /**
     * Input Find options. Where will change the different options.
     * Immutable as not setter is provided.
     */
    private final TableFindOptions tableFindOptions;

    /**
     * Cursor to iterate on the result of a query.
     *
     * @param dataSource
     *      source table
     * @param filter
     *      current filter
     * @param options
     *      options of the find operation
     * @param rowType
     *      row type returned with the cursor
     */
    public TableFindCursor(Table<T> dataSource, Filter filter, TableFindOptions options, Class<R> rowType) {
        super(rowType);
        this.filter = filter;
        this.dataSource = dataSource;
        this.tableFindOptions = options;
    }

    /**
     * Constructor by copy. Once cloning the cursor is set back at the beginning.
     *
     * @param tableFindCursor
     *      previous cursor
     */
    private TableFindCursor(TableFindCursor<T, R> tableFindCursor) {
        super(tableFindCursor.getRecordType());
        this.state            = CursorState.IDLE;
        this.buffer           = new ArrayList<>();
        this.consumed         = 0;
        this.currentPage      = null;
        this.dataSource       = tableFindCursor.dataSource;
        this.tableFindOptions = new TableFindOptions(tableFindCursor.tableFindOptions);
        this.filter           = tableFindCursor.filter;
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
        newTableFindCursor.tableFindOptions.limit(newLimit);
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
        newTableFindCursor.tableFindOptions.skip(newSkip);
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
        newTableFindCursor.tableFindOptions.includeSimilarity(true);
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
        newTableFindCursor.tableFindOptions.includeSortVector(true);
        return newTableFindCursor;
    }

    /**
     * Fetches the next batch of documents into the buffer.
     * This method handles paging, using the page state from the previous batch to fetch the next one.
     */
    public void fetchNextPage() {
        if (currentPage == null) {
            // Searching First Page
            currentPage = dataSource.findPage(filter, tableFindOptions, getRecordType());
            buffer.addAll(currentPage.getResults());
        } else if (currentPage.getPageState().isPresent()) {
            // Searching next page if exist
            tableFindOptions.pageState(currentPage.getPageState().get());
            currentPage = dataSource.findPage(filter, tableFindOptions, getRecordType());
            buffer.addAll(currentPage.getResults());
        } else {
            // Error ?
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