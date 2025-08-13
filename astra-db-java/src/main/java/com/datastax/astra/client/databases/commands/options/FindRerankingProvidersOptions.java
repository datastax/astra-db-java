package com.datastax.astra.client.databases.commands.options;

/*-
 * #%L
 * Data API Java Client
 * --
 * Copyright (C) 2024 - 2025 DataStax
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

import com.datastax.astra.client.admin.options.AdminOptions;
import com.datastax.astra.client.core.commands.CommandType;
import com.datastax.astra.client.core.options.BaseOptions;
import com.datastax.astra.client.core.vectorize.SupportModelStatus;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Represents the set of options available for the <code>findRerankingProviders</code> command.
 *
 * <p>This class allows configuration of execution parameters such as read concern, read preference,
 * and other command-level options relevant to querying reranking providers.</p>
 *
 * @see BaseOptions
 */
@Setter
@Accessors(fluent = true, chain = true)
public class FindRerankingProvidersOptions extends BaseOptions<FindRerankingProvidersOptions> {

    /**
     * Condition to upsert the table.
     */
    SupportModelStatus filterModelStatus = null;

    /**
     * Default constructor
     */
    public FindRerankingProvidersOptions() {
        super(null, CommandType.DATABASE_ADMIN, AdminOptions.DEFAULT_SERIALIZER, null);
    }

    /**
     * Accessor for serialization.
     *
     * @return
     *      accessor for serialization
     */
    public SupportModelStatus getFilterModelStatus() {
        return this.filterModelStatus;
    }

}
