package com.datastax.astra.client.model.tables.columns;

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

/**
 * Column Types
 */
public enum ColumnTypes {

    ASCII("ascii"),
    BIGINT("bigint"),
    BLOB("blob"),
    BOOLEAN("boolean"),
    DATE("date"),
    DECIMAL("decimal"),
    DOUBLE("double"),
    DURATION("duration"),
    FLOAT("float"),
    INET("inet"),
    INT("int"),
    LIST("list"),
    MAP("map"),
    SET("set"),
    SMALLINT("smallint"),
    TEXT("text"),
    TIME("time"),
    TIMESTAMP("timestamp"),
    TINYINT("tinyint"),
    VARINT("varint"),
    UUID("uuid"),
    VECTOR("vector");

    @Getter
    private final String value;

    ColumnTypes(String value) {
        this.value = value;
    }
}
