package com.datastax.astra.client.collections.commands.options;

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

import com.datastax.astra.client.core.options.BaseOptions;
import com.datastax.astra.client.core.query.Projection;
import com.datastax.astra.client.core.query.Sort;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * List Options for a FindOne command.
 */
@Getter @Setter
@Accessors(fluent = true, chain = true)
public class CollectionFindOneOptions extends BaseOptions<CollectionFindOneOptions> {

    /**
     * Order by.
     */
    Sort[] sort;

    /**
     * Select.
     */
    Projection[] projection;

    /**
     * Options.
     */
    Boolean includeSimilarity;

    /**
     * Flag to include sortVector in the result when operating a semantic search.
     */
    Boolean includeSortVector;

    /**
     * Default constructor.
     */
    public CollectionFindOneOptions() {
    }

    /**
     * Adding this on top of sort(Sort[] s) to allow for a more fluent API.
     * @param s
     *      sort options
     * @return
     *     current command
     */
    public CollectionFindOneOptions sort(Sort... s) {
        this.sort = s;
        return this;
    }

    /**
     * Adding this on top of projection(Projection[] p) to allow for a more fluent API.
     * @param p
     *      projection options
     * @return
     *     current command
     */
    public CollectionFindOneOptions projection(Projection... p) {
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
