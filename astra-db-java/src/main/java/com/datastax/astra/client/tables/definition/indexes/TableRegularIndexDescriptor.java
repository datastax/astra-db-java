package com.datastax.astra.client.tables.definition.indexes;

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

/**
 * Represents a descriptor for a table index, including the table's name and associated index options.
 * This class is designed for use in scenarios such as serialization/deserialization with libraries
 * like Jackson and for method chaining in fluent-style APIs.
 */
public class TableRegularIndexDescriptor extends TableIndexDescriptor<TableRegularIndexDefinition> {

    /**
     * The default name for the index.
     */
    public TableRegularIndexDescriptor() {
        super(TableRegularIndexDescriptor::new);
    }

    /**
     * Constructor that accepts a set of index options.
     *
     * @param definition the index options to use.
     */
    protected TableRegularIndexDescriptor(TableRegularIndexDefinition definition) {
        super(TableRegularIndexDescriptor::new);
        this.definition = definition;
    }

    @Override
    public TableRegularIndexDescriptor name(String name) {
        return (TableRegularIndexDescriptor) super.name(name);
    }

    @Override
    public TableRegularIndexDescriptor definition(TableRegularIndexDefinition def) {
        return (TableRegularIndexDescriptor) super.definition(def);
    }

}
