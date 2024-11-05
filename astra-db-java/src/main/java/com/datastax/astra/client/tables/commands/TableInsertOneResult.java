package com.datastax.astra.client.tables.commands;

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

import com.datastax.astra.client.tables.columns.ColumnDefinition;
import com.datastax.astra.client.tables.row.Row;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;

@Data @AllArgsConstructor
public class TableInsertOneResult {

    private ArrayList<Object> insertedId;

    private LinkedHashMap<String, ColumnDefinition> primaryKeySchema;

    /**
     * No-argument constructor that initializes {@code insertedIds} to an empty {@link ArrayList}
     *
     * and {@code primaryKeySchema} to an empty {@link LinkedHashMap}.
     */
    public TableInsertOneResult() {
        this.insertedId = new ArrayList<>();
        this.primaryKeySchema = new LinkedHashMap<>();
    }

    public Row getInsertedIdAsRow() {
        Row row = new Row();
        for (int i = 0; i < insertedId.size(); i++) {
            row.put(primaryKeySchema.keySet().toArray()[i].toString(), insertedId.get(i));
        }
        return row;
    }


}
