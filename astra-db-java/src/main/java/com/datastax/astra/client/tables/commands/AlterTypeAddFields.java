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
import com.datastax.astra.client.tables.definition.types.TableUserDefinedTypeFieldTypes;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents an operation to add columns to an existing table in a database schema.
 * This class implements the {@link AlterTableOperation} interface, providing functionality
 * to define new columns and their types for the "alter table add" operation.
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * AlterTypeAddFields operation = new AlterTypeAddFields()
 *     .addField("name", ColumnTypes.TEXT)
 *     .addFieldInt("age");
 * }
 * </pre>
 *
 * <p>Key Features:</p>
 * <ul>
 *   <li>Supports adding fields with various data types</li>
 *   <li>Chainable methods for building operations fluently.</li>
 * </ul>
 */
@Setter @Getter
public final class AlterTypeAddFields extends AlterTypeOperation<TableUserDefinedTypeFieldDefinition, AlterTypeAddFields> {

    /**
     * Constructs a new {@code AlterTableAddColumns} instance.
     */
    public AlterTypeAddFields() {
        super("add");
    }

}
