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
import lombok.Getter;
import lombok.Setter;

/**
 * Descriptor for a table vector index.
 */
@Getter @Setter
public class TableVectorIndexDescriptor extends TableIndexDescriptor<TableVectorIndexDefinition> {

    public TableVectorIndexDescriptor() {
        super(TableVectorIndexDescriptor::new);
    }

    /**
     * Constructor that accepts a set of index options.
     *
     * @param definition the index options to use.
     */
    protected TableVectorIndexDescriptor(TableVectorIndexDefinition definition) {
        super(TableVectorIndexDescriptor::new);
        this.definition = definition;
    }

    @Override
    public TableVectorIndexDescriptor name(String name) {
        return (TableVectorIndexDescriptor) super.name(name);
    }

    @Override
    public TableVectorIndexDescriptor definition(TableVectorIndexDefinition def) {
        return (TableVectorIndexDescriptor) super.definition(def);
    }
}
