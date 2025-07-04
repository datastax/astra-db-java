package com.datastax.astra.client.databases.commands.options;

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

import com.datastax.astra.client.admin.options.AdminOptions;
import com.datastax.astra.client.core.commands.CommandType;
import com.datastax.astra.client.core.options.BaseOptions;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Set of options used when creating a table
 */
@Setter
@Accessors(fluent = true, chain = true)
public class CreateKeyspaceOptions extends BaseOptions<CreateKeyspaceOptions> {

    /** Improve syntax. */
    public static final CreateKeyspaceOptions IF_NOT_EXISTS = new CreateKeyspaceOptions().ifNotExists(true);

    /**
     * Condition to upsert the table.
     */
    boolean ifNotExists = true;

    /**
     * Change the keyspace in the database.
     */
    boolean updateDBKeyspace = false;

    /**
     * Default constructor
     */
    public CreateKeyspaceOptions() {
        super(null, CommandType.DATABASE_ADMIN, AdminOptions.DEFAULT_SERIALIZER, null);
    }

    /**
     * Gets ifNotExists
     *
     * @return value of ifNotExists
     */
    public boolean isIfNotExists() {
        return ifNotExists;
    }

    /**
     * Gets updateDBKeyspace
     *
     * @return value of updateDBKeyspace
     */
    public boolean isUpdateDBKeyspace() {
        return updateDBKeyspace;
    }
}
