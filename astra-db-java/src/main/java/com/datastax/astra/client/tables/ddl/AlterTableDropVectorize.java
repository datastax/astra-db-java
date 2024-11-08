package com.datastax.astra.client.tables.ddl;

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

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
public class AlterTableDropVectorize implements AlterTableOperation {

    Boolean ifExists = null;

    Set<String> columns = new LinkedHashSet<>();

    public AlterTableDropVectorize(String... name) {
        this.columns.addAll(Arrays.asList(name));
    }

    public AlterTableDropVectorize(Set<String> columns) {
        this.columns.addAll(columns);
    }

    public AlterTableDropVectorize columns(String... columns) {
        this.columns.addAll(Arrays.asList(columns));
        return this;
    }

    public AlterTableDropVectorize ifExists() {
        this.ifExists = true;
        return this;
    }

    @Override
    public String getOperationName() {
        return "dropVectorize";
    }
}
