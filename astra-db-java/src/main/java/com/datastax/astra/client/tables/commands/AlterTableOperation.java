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

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Represents a generic operation to alter a database table schema.
 * This interface provides a contract for implementing specific schema alteration operations.
 * <p>
 * Implementations of this interface must define the name of the operation.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * AlterTableOperation operation = new AlterTableAddVectorize();
 * String operationName = operation.getOperationName();
 * }
 * </pre>
 */
public interface AlterTableOperation {

    /**
     * Returns the name of the table alteration operation.
     * This method is annotated with {@link com.fasterxml.jackson.annotation.JsonIgnore}
     * to prevent serialization of the operation name in JSON outputs.
     *
     * @return the name of the operation
     */
    @JsonIgnore
    String getOperationName();
}
