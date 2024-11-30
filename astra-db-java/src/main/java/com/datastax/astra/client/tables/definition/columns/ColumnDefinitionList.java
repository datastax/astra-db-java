package com.datastax.astra.client.tables.definition.columns;

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
import lombok.Setter;

@Getter @Setter
public class ColumnDefinitionList extends ColumnDefinition {

    private ColumnTypes valueType;

    public ColumnDefinitionList() {
        super(ColumnTypes.LIST);
    }

    public ColumnDefinitionList(ColumnTypes valueType) {
        this();
        this.valueType = valueType;
    }
}
