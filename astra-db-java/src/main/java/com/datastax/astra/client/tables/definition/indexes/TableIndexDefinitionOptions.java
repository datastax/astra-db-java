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

import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * Represents the options for table index definitions, allowing configuration of index characteristics.
 * This class uses {@link Boolean} to allow flags to be null, providing a tri-state logic
 * (true, false, or unspecified).
 *
 * <p>The available options are:</p>
 * <ul>
 *     <li><b>ascii:</b> Indicates whether the index should consider ASCII encoding.</li>
 *     <li><b>normalize:</b> Specifies if the index should normalize data.</li>
 *     <li><b>caseSensitive:</b> Determines if the index should be case-sensitive.</li>
 * </ul>
 */
@Data
public class TableIndexDefinitionOptions {

    /**
     * Indicates whether the index should consider ASCII encoding.
     */
    Boolean ascii;

    /**
     * Specifies if the index should normalize data.
     */
    Boolean normalize;

    /**
     * Determines if the index should be case-sensitive.
     */
    Boolean caseSensitive;

    /**
     * Default constructor.
     */
    public TableIndexDefinitionOptions() {
    }

    /**
     * Sets the ASCII flag for the index.
     *
     * @param ascii {@code true} to enable ASCII encoding, {@code false} to disable it.
     * @return the current instance for method chaining.
     */
    public TableIndexDefinitionOptions ascii(boolean ascii) {
        this.ascii = ascii;
        return this;
    }

    /**
     * Sets the normalization flag for the index.
     *
     * @param normalize {@code true} to enable normalization, {@code false} to disable it.
     * @return the current instance for method chaining.
     */
    public TableIndexDefinitionOptions normalize(boolean normalize) {
        this.normalize = normalize;
        return this;
    }

    /**
     * Sets the case sensitivity flag for the index.
     *
     * @param caseSensitive {@code true} to enable case sensitivity, {@code false} to disable it.
     * @return the current instance for method chaining.
     */
    public TableIndexDefinitionOptions caseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
        return this;
    }
}
