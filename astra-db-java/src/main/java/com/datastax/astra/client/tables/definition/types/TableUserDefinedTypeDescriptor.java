package com.datastax.astra.client.tables.definition.types;

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

import com.datastax.astra.internal.serdes.tables.RowSerializer;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents the User Defined Type definition with its name and metadata.
 */
@Getter @Setter
public class TableUserDefinedTypeDescriptor {

    /**
     * Name of the table.
     */
    private String udtName;

    /**
     * Options for the table.
     */
    private TableUserDefinedTypeDefinition definition;

    /**
     * Default constructor.
     */
    public TableUserDefinedTypeDescriptor() {
        // left blank, serialization with jackson
    }

    /**
     * Default constructor.
     * @param name the name of the table
     */
    public TableUserDefinedTypeDescriptor(String name) {
        // left blank, serialization with jackson
        this.udtName = name;
    }

    /**
     * Set the name of the table.
     * @param name the name of the table
     * @return the descriptor
     */
    public TableUserDefinedTypeDescriptor udtName(String name) {
        this.udtName = name;
        return this;
    }

    /**
     * Set the definition of the table.
     * @param def the definition of the table
     * @return the descriptor
     */
    public TableUserDefinedTypeDescriptor definition(TableUserDefinedTypeDefinition def) {
        this.definition = def;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return new RowSerializer().marshall(this);
    }
}
