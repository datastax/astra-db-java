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

import com.datastax.astra.internal.serdes.tables.AlterTypeOperationSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;

import java.util.LinkedHashMap;

/**
 * Represents a generic operation to alter a type schema.
 * This interface provides a contract for implementing specific schema alteration operations.
 * <p>
 * Implementations of this interface must define the name of the operation.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * AlterTypeOperation operation = new AlterTypeOperation();
 * String operationName = operation.getOperationName();
 * }
 * </pre>
 *
 * @param <FIELD_TYPE>
 *     the type of the fields in the operation.
 * @param <T>
 *         the type of the operation itself, used for method chaining.
 */
@Getter
@JsonSerialize(using = AlterTypeOperationSerializer.class)
public abstract class AlterTypeOperation<FIELD_TYPE, T extends AlterTypeOperation<?,?>> {

    /**
     * The Operation name.
     */
    final String operationName;

    /**
     * A map of column names to their definitions.
     * The map preserves the order of added columns.
     */
    LinkedHashMap<String, FIELD_TYPE> fields = new LinkedHashMap<>();

    /**
     * Gets the name of the operation.
     *
     * @param operationName
     *      the name of the operation.
     */
    public AlterTypeOperation(String operationName) {
        this.operationName = operationName;
    }

    /**
     * Adds a column with the specified name and type to the table.
     *
     * @param name the name of the column.
     * @param type the type of the column.
     * @return the current instance for chaining.
     */
    @SuppressWarnings("unchecked")
    public T addField(String name, FIELD_TYPE type) {
        fields.put(name, type);
        return (T) this;
    }

}
