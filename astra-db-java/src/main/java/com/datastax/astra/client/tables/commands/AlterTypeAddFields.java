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

import com.datastax.astra.client.tables.definition.types.TableUserDefinedTypeDefinition;
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

    /**
     * Adds a column to the table with a specific type.
     *
     * @param name the name of the column
     * @param type the type of the column
     * @return the updated {@link TableUserDefinedTypeDefinition} instance
     */
    public AlterTypeAddFields addField(String name, TableUserDefinedTypeFieldTypes type) {
        super.addField(name, new TableUserDefinedTypeFieldDefinition(type));
        return this;
    }

    /**
     * Adds a UUID column to the table.
     *
     * @param name the name of the column
     * @return the updated {@link TableUserDefinedTypeDefinition} instance
     */
    public AlterTypeAddFields addFieldUuid(String name) {
        return addField(name, TableUserDefinedTypeFieldTypes.UUID);
    }

    /**
     * Adds a text column to the table.
     *
     * @param name the name of the column
     * @return the updated {@link TableUserDefinedTypeDefinition} instance
     */
    public AlterTypeAddFields addFieldText(String name) {
        return addField(name, TableUserDefinedTypeFieldTypes.TEXT);
    }

    /**
     * Adds an ascii column to the table.
     *
     * @param name the name of the column
     * @return the updated {@link TableUserDefinedTypeDefinition} instance
     */
    public AlterTypeAddFields addFieldAscii(String name) {
        return addField(name, TableUserDefinedTypeFieldTypes.ASCII);
    }

    /**
     * Adds an date column to the table.
     *
     * @param name the name of the column
     * @return the updated {@link TableUserDefinedTypeDefinition} instance
     */
    public AlterTypeAddFields addFieldDate(String name) {
        return addField(name, TableUserDefinedTypeFieldTypes.DATE);
    }

    /**
     * Adds an integer column to the table.
     *
     * @param name the name of the column
     * @return the updated {@link TableUserDefinedTypeDefinition} instance
     */
    public AlterTypeAddFields addFieldInt(String name) {
        return addField(name, TableUserDefinedTypeFieldTypes.INT);
    }

    /**
     * Adds a timestamp column to the table.
     *
     * @param name the name of the column
     * @return the updated {@link TableUserDefinedTypeDefinition} instance
     */
    public AlterTypeAddFields addFieldTimestamp(String name) {
        return addField(name, TableUserDefinedTypeFieldTypes.TIMESTAMP);
    }

    /**
     * Adds a boolean column to the table.
     *
     * @param name the name of the column
     * @return the updated {@link TableUserDefinedTypeDefinition} instance
     */
    public AlterTypeAddFields addFieldBoolean(String name) {
        return addField(name, TableUserDefinedTypeFieldTypes.BOOLEAN);
    }

    /**
     * Adds a boolean column to the table.
     *
     * @param name the name of the column
     * @return the updated {@link TableUserDefinedTypeDefinition} instance
     */
    public AlterTypeAddFields addFieldBigInt(String name) {
        return addField(name, TableUserDefinedTypeFieldTypes.BIGINT);
    }

    /**
     * Adds a blob column to the table.
     *
     * @param name the name of the column
     * @return the updated {@link TableUserDefinedTypeDefinition} instance
     */
    public AlterTypeAddFields addFieldBlob(String name) {
        return addField(name, TableUserDefinedTypeFieldTypes.BLOB);
    }

}
