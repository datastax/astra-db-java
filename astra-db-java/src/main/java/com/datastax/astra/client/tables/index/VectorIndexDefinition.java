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

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Index Definitions.
 */
@Data @NoArgsConstructor
public class VectorIndexDefinition {

    String column;

    VectorIndexDefinitionOptions options;

    public VectorIndexDefinition column(String column) {
        this.column = column;
        return this;
    }

    public VectorIndexDefinition options(VectorIndexDefinitionOptions options) {
        this.options = options;
        return this;
    }

}
