package com.datastax.astra.client.core.options;

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

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@NoArgsConstructor
@Accessors(fluent = true, chain = true)
public class TimeoutOptions {

    public static final long DEFAULT_REQUEST_TIMEOUT_MILLIS          = 5000;
    public static final long DEFAULT_DATA_OPERATION_TIMEOUT_MILLIS   = 30000;
    public static final long DEFAULT_SCHEMA_OPERATION_TIMEOUT_MILLIS = 100000;
    public static final long DEFAULT_KEYSPACE_ADMIN_TIMEOUT_MILLIS   = 30000;
    public static final long DEFAULT_DATABASE_ADMIN_TIMEOUT_MILLIS   = 1000000;

    long requestTimeoutMillis = DEFAULT_REQUEST_TIMEOUT_MILLIS;

    long dataOperationTimeoutMillis = DEFAULT_DATA_OPERATION_TIMEOUT_MILLIS;

    long schemaOperationTimeoutMillis = DEFAULT_SCHEMA_OPERATION_TIMEOUT_MILLIS;

    long databaseAdminTimeoutMillis = DEFAULT_DATABASE_ADMIN_TIMEOUT_MILLIS;

    long keyspaceAdminTimeoutMillis =- DEFAULT_KEYSPACE_ADMIN_TIMEOUT_MILLIS;
}
