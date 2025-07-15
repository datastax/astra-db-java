package com.datastax.astra.client.tables.commands;

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

import com.datastax.astra.client.tables.definition.types.TableUserDefinedTypeFieldDefinition;

import java.util.LinkedHashMap;

/**
 * Represents an operation to rename columns in a database table.
 * This class implements the {@link AlterTableOperation} interface,
 * providing the functionality to return the operation name as "rename".
 *
 * <p>This operation is typically used to rename one or more columns in a table
 * during schema alterations.</p>
 */
public class AlterTypeRenameFields extends AlterTypeOperation<String, AlterTypeRenameFields> {

    /**
     * Constructs a new {@code AlterTableAddColumns} instance.
     */
    public AlterTypeRenameFields() {
        super("rename");
    }

}
