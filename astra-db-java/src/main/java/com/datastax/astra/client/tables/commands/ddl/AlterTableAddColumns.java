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

import com.datastax.astra.client.tables.TableDefinition;
import com.datastax.astra.client.tables.columns.ColumnDefinition;
import com.datastax.astra.client.tables.columns.ColumnDefinitionList;
import com.datastax.astra.client.tables.columns.ColumnDefinitionMap;
import com.datastax.astra.client.tables.columns.ColumnDefinitionSet;
import com.datastax.astra.client.tables.columns.ColumnDefinitionVector;
import com.datastax.astra.client.tables.columns.ColumnTypes;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashMap;

@Setter @Getter
@NoArgsConstructor
public final class AlterTableAddColumns implements AlterTableOperation {

    Boolean ifNotExists = null;

    LinkedHashMap<String, ColumnDefinition> columns = new LinkedHashMap<>();

    @Override
    public String getOperationName() {
        return "add";
    }

    /**
     * Add a column to the table.
     *
     * @param name
     *      name of the column
     * @param type
     *      type of the column
     * @return
     *      the current instance
     */
    public AlterTableAddColumns addColumn(String name, ColumnTypes type) {
        columns.put(name, new ColumnDefinition(type));
        return this;
    }

    public AlterTableAddColumns addColumnVector(String name, ColumnDefinitionVector cdv) {
        columns.put(name, cdv);
        return this;
    }

    public AlterTableAddColumns addColumnText(String name) {
        return addColumn(name, ColumnTypes.TEXT);
    }

    public AlterTableAddColumns addColumnInt(String name) {
        return addColumn(name, ColumnTypes.INT);
    }

    public AlterTableAddColumns addColumnBoolean(String name) {
        return addColumn(name, ColumnTypes.BOOLEAN);
    }

    public AlterTableAddColumns addColumnList(String name, ColumnTypes valueType) {
        columns.put(name, new ColumnDefinitionList(valueType));
        return this;
    }

    public AlterTableAddColumns addColumnSet(String name, ColumnTypes valueType) {
        columns.put(name, new ColumnDefinitionSet(valueType));
        return this;
    }

    public AlterTableAddColumns addColumnMap(String name,  ColumnTypes keyType, ColumnTypes valueType) {
        columns.put(name, new ColumnDefinitionMap(keyType, valueType));
        return this;
    }

    public AlterTableAddColumns ifNotExists() {
        this.ifNotExists = true;
        return this;
    }

}
