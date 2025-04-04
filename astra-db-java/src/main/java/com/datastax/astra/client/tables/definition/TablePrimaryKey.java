package com.datastax.astra.client.tables.definition;

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

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * The primary key definition for a table.
 */
@Data
public class TablePrimaryKey {

    /**
     * The primary key columns.
     */
    @JsonProperty("partitionBy")
    private List<String> partitionBy = new ArrayList<>();

    /**
     * The clustering columns.
     */
    @JsonProperty("partitionSort")
    private LinkedHashMap<String, Integer> partitionSort;

    /**
     * Default constructor.
     */
    public TablePrimaryKey() {}
}
