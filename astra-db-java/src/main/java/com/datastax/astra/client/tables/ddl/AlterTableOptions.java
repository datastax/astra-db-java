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

import com.datastax.astra.client.core.commands.BaseOptions;
import com.datastax.astra.client.core.commands.CommandType;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import static com.datastax.astra.client.tables.Table.DEFAULT_TABLE_SERIALIZER;

@Setter
@Accessors(fluent = true, chain = true)
public class AlterTableOptions extends BaseOptions<AlterTableOptions> {

    /** Improve syntax. */
    public static final AlterTableOptions IF_EXISTS = new AlterTableOptions().ifExists(true);

    /**
     * Condition to upsert the table.
     */
    boolean ifExists = true;

    public AlterTableOptions() {
        super(null, CommandType.TABLE_ADMIN, DEFAULT_TABLE_SERIALIZER, null);
    }

    /**
     * Accessor for serialization.
     *
     * @return
     *      accessor for serialization
     */
    public boolean isIfExists() {
        return ifExists;
    }

}
