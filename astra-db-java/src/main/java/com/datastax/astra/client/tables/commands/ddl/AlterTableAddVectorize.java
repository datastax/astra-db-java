package com.datastax.astra.client.tables.commands.ddl;

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

import com.datastax.astra.client.core.vector.VectorServiceOptions;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@Setter
@Getter
public class AlterTableAddVectorize  implements AlterTableOperation {

    Boolean ifNotExists = null;

    Map<String, VectorServiceOptions> columns = new HashMap<>();

    @Override
    public String getOperationName() {
        return "addVectorize";
    }

    public AlterTableAddVectorize addColumn(String name, VectorServiceOptions def) {
        columns.put(name, def);
        return this;
    }

    public AlterTableAddVectorize columns(Map<String, VectorServiceOptions> columns) {
        this.columns = columns;
        return this;
    }

    public AlterTableAddVectorize ifNotExists() {
        this.ifNotExists = true;
        return this;
    }

}
