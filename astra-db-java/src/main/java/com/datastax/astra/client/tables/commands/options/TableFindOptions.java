package com.datastax.astra.client.tables.commands.options;

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

import com.datastax.astra.client.core.commands.CommandType;
import com.datastax.astra.client.core.options.BaseOptions;
import com.datastax.astra.client.core.query.Projection;
import com.datastax.astra.client.core.query.Sort;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import static com.datastax.astra.client.tables.Table.DEFAULT_TABLE_SERIALIZER;

/**
 * List Options for a FindOne command.
 */
@Getter @Setter
@Accessors(fluent = true, chain = true)
public class TableFindOptions extends BaseOptions<TableFindOptions> {

    /**
     * Projections
     */
    Projection[] projection;

    /**
     * List of sorts
     */
    Sort[] sort;

    /**
     * Skip a few result in the beginning
     */
    Integer skip;

    /**
     * Stop processing after a few results
     */
    Integer limit;

    /**
     * Flag to include similarity in the result when operating a semantic search.
     */
    Boolean includeSimilarity;

    /**
     * Flag to include sortVector in the result when operating a semantic search.
     */
    Boolean includeSortVector;

    /**
     * Page state.
     */
    String pageState;

    /**
     * Default constructor.
     */
    public TableFindOptions() {
        super(null, CommandType.GENERAL_METHOD, DEFAULT_TABLE_SERIALIZER, null);
    }

    /**
     * Add a skip clause in the find block
     *
     * @param skip value for skip options
     * @return current command
     */
    public TableFindOptions skip(int skip) {
        if (skip < 0) {
            throw new IllegalArgumentException("Skip must be positive");
        }
        this.skip = skip;
        return this;
    }

    /**
     * Add a limit clause in the find block
     *
     * @param limit value for limit options
     * @return current command
     */
    public TableFindOptions limit(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("Limit must be positive");
        }
        this.limit = limit;
        return this;
    }

    /**
     * Adding this on top of sort(Sort[] s) to allow for a more fluent API.
     * @param s
     *      sort options
     * @return
     *     current command
     */
    public TableFindOptions sort(Sort... s) {
        this.sort = s;
        return this;
    }

    /**
     * Adding this on top of sort(Sort[] s) to allow for a more fluent API.
     * @param p
     *      projection options
     * @return
     *     current command
     */
    public TableFindOptions projection(Projection... p) {
        this.projection = p;
        return this;
    }


    /**
     * Get the sort options.
     *
     * @return
     *      sort options
     */
    public Sort[] getSortArray() {
        return sort;
    }

    /**
     * Adding this on top of sort(Sort[] s) to allow for a more fluent API.
     *
     * @return
     *      project
     */
    public Projection[] getProjectionArray() {
        return projection;
    }

}
