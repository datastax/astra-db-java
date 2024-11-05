package com.datastax.astra.client.tables.index;

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

@Getter
@Setter
public class IndexDescriptor {

    /**
     * Name of the table.
     */
    private String name;

    /**
     * Options for the table.
     */
    private IndexDefinition definition;

    /**
     * Default constructor.
     */
    public IndexDescriptor() {
        // left blank, serialization with jackson
    }

    /**
     * Default constructor.
     */
    public IndexDescriptor(String name) {
        // left blank, serialization with jackson
        this.name = name;
    }

    public IndexDescriptor name(String name) {
        this.name = name;
        return this;
    }

    public IndexDescriptor definition(IndexDefinition def) {
        this.definition = def;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return new RowSerializer().marshall(this);
    }
}
