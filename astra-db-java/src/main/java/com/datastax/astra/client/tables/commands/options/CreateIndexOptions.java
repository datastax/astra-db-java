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

import com.datastax.astra.client.core.options.BaseOptions;
import lombok.Setter;
import lombok.experimental.Accessors;

import static com.datastax.astra.client.core.commands.CommandType.TABLE_ADMIN;
import static com.datastax.astra.client.tables.Table.DEFAULT_TABLE_SERIALIZER;

/**
 * Set of options used when creating a table
 */
@Setter
@Accessors(fluent = true, chain = true)
public class CreateIndexOptions extends BaseOptions<CreateIndexOptions> {

    /** Improve syntax. */
    public static final CreateIndexOptions IF_NOT_EXISTS = new CreateIndexOptions().ifNotExists(true);

    /**
     * Condition to upsert the table.
     */
    boolean ifNotExists = true;

    /**
     * Default constructor
     */
    public CreateIndexOptions() {
        super(null, TABLE_ADMIN, DEFAULT_TABLE_SERIALIZER, null);
    }

    /**
     * Accessor for serialization.
     *
     * @return
     *      accessor for serialization
     */
    public boolean isIfNotExists() {
        return ifNotExists;
    }

}
