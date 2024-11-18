package com.datastax.astra.client.tables.ddl;

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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Set of options used when creating a table
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(fluent = true, chain = true)
public class CreateIndexOptions {

    /** Improve syntax. */
    public static final CreateIndexOptions IF_NOT_EXISTS = new CreateIndexOptions().ifNotExists(true);

    /**
     * Condition to upsert the table.
     */
    boolean ifNotExists = true;

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
