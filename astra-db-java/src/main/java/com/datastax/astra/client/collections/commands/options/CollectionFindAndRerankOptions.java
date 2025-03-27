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

import com.datastax.astra.client.core.commands.CommandType;
import com.datastax.astra.client.core.hybrid.Hybrid;
import com.datastax.astra.client.core.hybrid.HybridLimits;
import com.datastax.astra.client.core.options.BaseOptions;
import com.datastax.astra.client.core.query.Projection;
import com.datastax.astra.client.core.query.Sort;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Map;

import static com.datastax.astra.client.collections.Collection.DEFAULT_COLLECTION_SERIALIZER;

/**
 * List Options for a FindOne command.
 */
@Getter @Setter
@Accessors(fluent = true, chain = true)
public class CollectionFindAndRerankOptions extends BaseOptions<CollectionFindAndRerankOptions> {

    /**
     * Order by.
     */
    Sort[] sort;

    /**
     * Projection for return document (select)
     */
    Projection[] projection;

    /**
     * Skip a few result in the beginning
     */
    HybridLimits hybridLimits;

    /**
     * Stop processing after a few results
     */
    Integer limit;

    /**
     * Options for Rerank on.
     */
    String rerankOn;

    /**
     * Options for hybrid projection
     */
    Boolean includeScores;

    /**
     * Flag to include sortVector in the result when operating a semantic search.
     */
    Boolean includeSortVector;

    Boolean includeSimilarity;

    /**
     * Default constructor.
     */
    public CollectionFindAndRerankOptions() {
        super(null, CommandType.GENERAL_METHOD, DEFAULT_COLLECTION_SERIALIZER, null);
    }

    /**
     * Adding this on top of sort(Sort[] s) to allow for a more fluent API.
     *
     * @param hybrid
     *      hybrid
     * @return
     *     current command
     */
    public CollectionFindAndRerankOptions hybridSort(Hybrid hybrid) {
        return sort(Sort.hybrid(hybrid));
    }

    /**
     * Adding this on top of sort(Sort[] s) to allow for a more fluent API.
     *
     * @param sorts
     *      sorts criteria
     * @return
     *     current command
     */
    public CollectionFindAndRerankOptions sort(Sort... sorts) {
        this.sort = sorts;
        return this;
    }

    public Sort[] getSortArray() {
        return this.sort;
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

    /**
     * Adding this on top of projection(Projection[] p) to allow for a more fluent API.
     * @param p
     *      projection options
     * @return
     *     current command
     */
    public CollectionFindAndRerankOptions projection(Projection... p) {
        this.projection = p;
        return this;
    }

    /**
     * Add a limit clause in the find block
     *
     * @param limit value for limit options
     * @return current command
     */
    public CollectionFindAndRerankOptions limit(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("Limit must be positive");
        }
        this.limit = limit;
        return this;
    }

    /**
     * Add a hybridLimits clause in the find block
     *
     * @param hybridLimits value for limit options
     * @return current command
     */
    public CollectionFindAndRerankOptions hybridLimits(int hybridLimits) {
        if (limit < 0) {
            throw new IllegalArgumentException("HybridLimits must be positive");
        }
        this.hybridLimits = new HybridLimits(hybridLimits);
        return this;
    }

    /**
     * Add a hybridLimits clause in the find block
     *
     * @param hybridMapLimits value for limit options
     * @return current command
     */
    public CollectionFindAndRerankOptions hybridLimits(Map<String, Integer> hybridMapLimits) {
        this.hybridLimits = new HybridLimits(hybridMapLimits);
        return this;
    }

    /**
     * Add a rerankOn clause in the find block
     *
     * @param rerankOn value for rerankOn options
     * @return current command
     */
    public CollectionFindAndRerankOptions rerankOn(String rerankOn) {
        this.rerankOn = rerankOn;
        return this;
    }


}
